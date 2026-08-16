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
    private final Map<String, ElectronicInvoicingProvider> providersMap;

    @Value("${billing.provider:factus}")
    private String activeProviderName;

    public BillingServiceImpl(
            ElectronicInvoiceRepository repository,
            BillingCanonicalMapper canonicalMapper,
            ElectronicInvoiceMapper invoiceMapper,
            @Lazy SaleFacade saleFacade,
            List<ElectronicInvoicingProvider> providers
    ) {
        this.repository = repository;
        this.canonicalMapper = canonicalMapper;
        this.invoiceMapper = invoiceMapper;
        this.saleFacade = saleFacade;
        this.providersMap = providers.stream()
                .collect(Collectors.toMap(
                        p -> p.getProviderName().toLowerCase(),
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
    }

    @Override
    public ElectronicInvoiceDto processElectronicInvoice(SaleDto sale) {
        Objects.requireNonNull(sale, "El objeto SaleDto no puede ser nulo");
        if (sale.id() == null) {
            throw new IllegalArgumentException("El ID de la venta es obligatorio para facturar");
        }

        // 1. Obtener o crear registro inicial de factura
        ElectronicInvoice einvoice = getOrCreateInitialInvoice(sale.id());

        // Guard de idempotencia: Si ya está validada, no volver a emitir
        if ("VALIDATED".equalsIgnoreCase(einvoice.getStatus())) {
            log.info("La venta ID {} ya posee una factura electrónica VALIDATED ({})", sale.id(), einvoice.getFactusNumber());
            return invoiceMapper.toDto(einvoice);
        }

        // 2. Resolver la estrategia activa de facturación (Strategy Pattern)
        ElectronicInvoicingProvider provider = resolveProvider();

        // 3. Mapear a solicitud canónica agnóstica
        InvoiceRequest request = canonicalMapper.toInvoiceRequest(sale);

        // 4. Ejecutar llamada I/O a través del adaptador fuera de la transacción principal
        InvoiceResult result;
        try {
            result = provider.emitInvoice(request);
        } catch (Exception ex) {
            log.error("Error al ejecutar emision con proveedor {}: {}", provider.getProviderName(), ex.getMessage());
            result = InvoiceResult.error("Error en proveedor " + provider.getProviderName() + ": " + ex.getMessage());
        }

        // 5. Aplicar resultados normalizados en la entidad
        applyResultToEntity(einvoice, result);

        // 6. Persistir estado en BD
        ElectronicInvoice saved = updateInvoiceStatus(einvoice);
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

        return processElectronicInvoice(sale);
    }

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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ElectronicInvoice updateInvoiceStatus(ElectronicInvoice einvoice) {
        return repository.save(einvoice);
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
