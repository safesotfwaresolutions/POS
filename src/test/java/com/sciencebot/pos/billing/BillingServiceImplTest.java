package com.sciencebot.pos.billing;

import com.sciencebot.pos.billing.internal.adapters.ElectronicInvoicingProvider;
import com.sciencebot.pos.billing.internal.adapters.dto.CustomerBillingData;
import com.sciencebot.pos.billing.internal.adapters.dto.InvoiceItemData;
import com.sciencebot.pos.billing.internal.adapters.dto.InvoiceRequest;
import com.sciencebot.pos.billing.internal.adapters.mock.MockBillingAdapter;
import com.sciencebot.pos.billing.internal.entities.ElectronicInvoice;
import com.sciencebot.pos.billing.internal.mappers.BillingCanonicalMapper;
import com.sciencebot.pos.billing.internal.mappers.ElectronicInvoiceMapper;
import com.sciencebot.pos.billing.internal.repositories.ElectronicInvoiceRepository;
import com.sciencebot.pos.billing.internal.services.BillingServiceImpl;
import com.sciencebot.pos.sales.SaleDto;
import com.sciencebot.pos.sales.SaleFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BillingServiceImplTest {

    @Mock
    private ElectronicInvoiceRepository repository;

    @Mock
    private BillingCanonicalMapper canonicalMapper;

    @Mock
    private ElectronicInvoiceMapper invoiceMapper;

    @Mock
    private SaleFacade saleFacade;

    @Mock
    private ElectronicInvoicingProvider customProvider;

    private MockBillingAdapter mockAdapter;
    private BillingServiceImpl billingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockAdapter = new MockBillingAdapter();

        when(customProvider.getProviderName()).thenReturn("factus");

        billingService = new BillingServiceImpl(
                repository,
                canonicalMapper,
                invoiceMapper,
                saleFacade,
                List.of(mockAdapter, customProvider)
        );
    }

    @Test
    void processElectronicInvoice_WithMockProvider_Success() {
        ReflectionTestUtils.setField(billingService, "activeProviderName", "mock");

        SaleDto saleDto = new SaleDto(1L, "FACT-000001", null, "Cliente General", BigDecimal.valueOf(50.0), BigDecimal.valueOf(50.0), BigDecimal.ZERO, "admin", List.of());
        ElectronicInvoice pendingEntity = new ElectronicInvoice();
        pendingEntity.setId(10L);
        pendingEntity.setSaleId(1L);
        pendingEntity.setStatus("PENDING");

        when(repository.findBySaleId(1L)).thenReturn(Optional.of(pendingEntity));
        when(repository.save(any(ElectronicInvoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerBillingData customerData = new CustomerBillingData("222222222222", "", "Consumidor Final", "a@a.com", 13, 21, null);
        InvoiceItemData itemData = new InvoiceItemData("REF-PROD", "Producto 1", 1, BigDecimal.valueOf(50.0), BigDecimal.ZERO, BigDecimal.valueOf(50.0));
        InvoiceRequest request = new InvoiceRequest(1L, "FACT-000001", "10", customerData, List.of(itemData), BigDecimal.valueOf(50.0));
        when(canonicalMapper.toInvoiceRequest(saleDto)).thenReturn(request);

        ElectronicInvoiceDto expectedDto = new ElectronicInvoiceDto(10L, 1L, "SETP-MOCK-000001", "mock-cufe", "mock-qr", "VALIDATED", null, "mock-pdf", LocalDateTime.now());
        when(invoiceMapper.toDto(any(ElectronicInvoice.class))).thenReturn(expectedDto);

        ElectronicInvoiceDto result = billingService.processElectronicInvoice(saleDto);

        assertNotNull(result);
        assertEquals("VALIDATED", result.status());
        assertEquals("SETP-MOCK-000001", result.factusNumber());
        verify(repository, atLeastOnce()).save(any(ElectronicInvoice.class));
    }

    @Test
    void processElectronicInvoice_AlreadyValidated_DoesNotCallProvider() {
        SaleDto saleDto = new SaleDto(1L, "FACT-000001", null, "Cliente General", BigDecimal.valueOf(50.0), BigDecimal.valueOf(50.0), BigDecimal.ZERO, "admin", List.of());
        ElectronicInvoice validatedEntity = new ElectronicInvoice();
        validatedEntity.setId(10L);
        validatedEntity.setSaleId(1L);
        validatedEntity.setStatus("VALIDATED");
        validatedEntity.setFactusNumber("SETP-001");

        when(repository.findBySaleId(1L)).thenReturn(Optional.of(validatedEntity));
        ElectronicInvoiceDto validatedDto = new ElectronicInvoiceDto(10L, 1L, "SETP-001", "cufe", "qr", "VALIDATED", null, "pdf", LocalDateTime.now());
        when(invoiceMapper.toDto(validatedEntity)).thenReturn(validatedDto);

        ElectronicInvoiceDto result = billingService.processElectronicInvoice(saleDto);

        assertNotNull(result);
        assertEquals("VALIDATED", result.status());
        verify(customProvider, never()).emitInvoice(any());
        verify(canonicalMapper, never()).toInvoiceRequest(any());
    }

    @Test
    void retryInvoice_AlreadyValidated_ThrowsException() {
        SaleDto saleDto = new SaleDto(1L, "FACT-000001", null, "Cliente General", BigDecimal.valueOf(50.0), BigDecimal.valueOf(50.0), BigDecimal.ZERO, "admin", List.of());
        when(saleFacade.getById(1L)).thenReturn(Optional.of(saleDto));

        ElectronicInvoice validatedEntity = new ElectronicInvoice();
        validatedEntity.setId(10L);
        validatedEntity.setSaleId(1L);
        validatedEntity.setStatus("VALIDATED");
        when(repository.findBySaleId(1L)).thenReturn(Optional.of(validatedEntity));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> billingService.retryInvoice(1L));

        assertTrue(ex.getMessage().contains("ya está validada"));
    }
}
