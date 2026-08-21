package com.sciencebot.pos.billing.internal.services;

import com.sciencebot.pos.billing.BillingFacade;
import com.sciencebot.pos.billing.ElectronicInvoiceDto;
import com.sciencebot.pos.billing.internal.adapters.ElectronicInvoicingProvider;
import com.sciencebot.pos.billing.internal.adapters.dto.InvoiceRequest;
import com.sciencebot.pos.billing.internal.adapters.dto.InvoiceResult;
import com.sciencebot.pos.billing.internal.entities.ElectronicInvoice;
import com.sciencebot.pos.billing.internal.mappers.BillingCanonicalMapper;
import com.sciencebot.pos.billing.internal.mappers.ElectronicInvoiceMapper;
import com.sciencebot.pos.billing.internal.repositories.ElectronicInvoiceRepository;
import com.sciencebot.pos.sales.SaleDto;
import com.sciencebot.pos.sales.SaleFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Servicio orquestador de Facturación Electrónica.
 * Desacoplado de proveedores externos mediante el patrón Strategy (ElectronicInvoicingProvider).
 */
@Service
public class BillingServiceImpl implements BillingFacade {

    private static final Logger log = LoggerFactory.getLogger(BillingServiceImpl.class);

    private final ElectronicInvoiceRepository repository;
    private final BillingCanonicalMapper canonicalMapper;
    private final ElectronicInvoiceMapper invoiceMapper;
    private final SaleFacade saleFacade;
    /** Proxy propio: permite que retryInvoice atraviese el interceptor transaccional. */
    private BillingFacade self;
    private final Map<String, ElectronicInvoicingProvider> providersMap;

    @Value("${billing.provider:factus}")
    private String activeProviderName;

    public BillingServiceImpl(
            ElectronicInvoiceRepository repository,
            BillingCanonicalMapper canonicalMapper,
            ElectronicInvoiceMapper invoiceMapper,
            @Lazy SaleFacade saleFacade,
            @Lazy BillingFacade self,
            List<ElectronicInvoicingProvider> providers
    ) {
        this.repository = repository;
        this.canonicalMapper = canonicalMapper;
        this.invoiceMapper = invoiceMapper;
        this.saleFacade = saleFacade;
        this.self = self;
        this.providersMap = providers.stream()
                .collect(Collectors.toMap(
                        p -> p.getProviderName().toLowerCase(),
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
    }

    /**
     * Emite (o reintenta) la factura electrónica de una venta.
     *
     * <p>Todo el flujo corre en una única transacción que mantiene un bloqueo pesimista sobre
     * la fila de la factura mientras dura la emisión. Esto serializa las emisiones concurrentes
     * de una misma venta (p. ej. venta con {@code sendToFactus} + reintento manual al tiempo) y
     * garantiza que el guard {@code VALIDATED} se evalúe sobre un estado consistente, evitando
     * emitir dos veces la misma factura legal ante la DIAN. El bloqueo es por fila (por venta),
     * de modo que facturas de ventas distintas no compiten entre sí.
     *
     * <p>Usa {@code REQUIRED}: cuando lo dispara una venta se une a su transacción (así el
     * {@code INSERT} en electronic_invoices ve la venta recién persistida y respeta la FK).
     * Un fallo del proveedor NO revierte la venta porque la excepción de I/O se captura aquí
     * y se traduce a una factura en estado {@code ERROR} reintentable; nunca escapa para marcar
     * la transacción como rollback-only.
     */
    @Override
    @Transactional
    public ElectronicInvoiceDto processElectronicInvoice(SaleDto sale) {
        Objects.requireNonNull(sale, "El objeto SaleDto no puede ser nulo");
        if (sale.id() == null) {
            throw new IllegalArgumentException("El ID de la venta es obligatorio para facturar");
        }

        // 1. Obtener (con bloqueo) o crear el registro de factura de esta venta.
        ElectronicInvoice einvoice = lockOrCreateInvoice(sale.id());

        // 2. Guard de idempotencia bajo bloqueo: si ya está validada, no volver a emitir.
        if ("VALIDATED".equalsIgnoreCase(einvoice.getStatus())) {
            log.info("La venta ID {} ya posee una factura electrónica VALIDATED ({})", sale.id(), einvoice.getFactusNumber());
            return invoiceMapper.toDto(einvoice);
        }

        // 3. Resolver la estrategia activa de facturación (Strategy Pattern)
        ElectronicInvoicingProvider provider = resolveProvider();

        // 4. Mapear a solicitud canónica agnóstica
        InvoiceRequest request = canonicalMapper.toInvoiceRequest(sale);

        // 5. Ejecutar la llamada I/O al proveedor (el bloqueo de la fila serializa reintentos).
        InvoiceResult result;
        try {
            result = provider.emitInvoice(request);
        } catch (Exception ex) {
            log.error("Error al ejecutar emision con proveedor {}: {}", provider.getProviderName(), ex.getMessage());
            result = InvoiceResult.error("Error en proveedor " + provider.getProviderName() + ": " + ex.getMessage());
        }

        // 6. Aplicar resultados normalizados y persistir en la misma transacción.
        applyResultToEntity(einvoice, result);
        ElectronicInvoice saved = repository.save(einvoice);
        return invoiceMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ElectronicInvoiceDto> getBySaleId(Long saleId) {
        if (saleId == null) {
            return Optional.empty();
        }
        if (saleFacade.getById(saleId).isEmpty()) {
            return Optional.empty();
        }
        ElectronicInvoice einvoice = getOrCreateInitialInvoice(saleId);
        return Optional.ofNullable(invoiceMapper.toDto(einvoice));
    }

    @Override
    public ElectronicInvoiceDto retryInvoice(Long saleId) {
        if (saleId == null) {
            throw new IllegalArgumentException("El ID de la venta es obligatorio");
        }

        SaleDto sale = saleFacade.getById(saleId)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada con ID: " + saleId));

        Optional<ElectronicInvoice> existingOpt = repository.findBySaleId(saleId);
        if (existingOpt.isPresent() && "VALIDATED".equalsIgnoreCase(existingOpt.get().getStatus())) {
            throw new IllegalArgumentException("La factura ya está validada y no puede reintentarse");
        }

        // Vía el proxy para que aplique @Transactional(REQUIRES_NEW) y el bloqueo pesimista;
        // una llamada directa (this.) se saltaría el interceptor transaccional.
        return self.processElectronicInvoice(sale);
    }

    /**
     * Obtiene la factura de la venta con bloqueo pesimista, o la crea si no existe.
     * Se ejecuta dentro de la transacción de {@link #processElectronicInvoice} para
     * mantener el bloqueo durante toda la emisión. El {@code saveAndFlush} en la creación
     * fuerza la restricción única {@code sale_id}: si dos emisiones iniciales concurren,
     * la perdedora falla en el flush ANTES de llamar al proveedor (no hay doble emisión).
     */
    private ElectronicInvoice lockOrCreateInvoice(Long saleId) {
        return repository.findBySaleIdForUpdate(saleId)
                .orElseGet(() -> {
                    ElectronicInvoice newInv = new ElectronicInvoice();
                    newInv.setSaleId(saleId);
                    newInv.setStatus("PENDING");
                    return repository.saveAndFlush(newInv);
                });
    }

    /** Variante de solo lectura para consultas (getBySaleId): no bloquea. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ElectronicInvoice getOrCreateInitialInvoice(Long saleId) {
        return repository.findBySaleId(saleId)
                .orElseGet(() -> {
                    ElectronicInvoice newInv = new ElectronicInvoice();
                    newInv.setSaleId(saleId);
                    newInv.setStatus("PENDING");
                    return repository.save(newInv);
                });
    }

    private ElectronicInvoicingProvider resolveProvider() {
        String key = (activeProviderName != null) ? activeProviderName.trim().toLowerCase() : "factus";
        ElectronicInvoicingProvider provider = providersMap.get(key);
        if (provider == null) {
            log.warn("Proveedor de facturación '{}' no encontrado. Utilizando proveedor por defecto 'mock'", key);
            provider = providersMap.get("mock");
        }
        if (provider == null) {
            throw new IllegalStateException("No hay ningún proveedor de facturación configurado o disponible.");
        }
        return provider;
    }

    private void applyResultToEntity(ElectronicInvoice einvoice, InvoiceResult result) {
        if (result != null) {
            if (result.legalInvoiceNumber() != null) {
                einvoice.setFactusNumber(result.legalInvoiceNumber());
            }
            if (result.cufe() != null) {
                einvoice.setCufe(result.cufe());
            }
            if (result.qrCode() != null) {
                einvoice.setQrCode(result.qrCode());
            }
            if (result.pdfUrl() != null) {
                einvoice.setPdfUrl(result.pdfUrl());
            }
            if (result.status() != null) {
                einvoice.setStatus(result.status());
            }
            einvoice.setErrorMessage(result.errorMessage());
            if ("VALIDATED".equalsIgnoreCase(result.status())) {
                einvoice.setValidatedAt(result.validatedAt() != null ? result.validatedAt() : LocalDateTime.now());
            }
        }
    }
}
