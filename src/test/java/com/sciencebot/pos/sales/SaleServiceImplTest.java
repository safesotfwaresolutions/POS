package com.sciencebot.pos.sales;

import com.sciencebot.pos.sales.internal.entities.Sale;
import com.sciencebot.pos.sales.internal.repositories.SaleRepository;
import com.sciencebot.pos.sales.internal.services.SaleServiceImpl;
import com.sciencebot.pos.sales.internal.mappers.SaleMapper;
import com.sciencebot.pos.customers.CustomerDto;
import com.sciencebot.pos.customers.CustomerFacade;
import com.sciencebot.pos.products.ProductDto;
import com.sciencebot.pos.products.ProductFacade;
import com.sciencebot.pos.inventory.InventoryFacade;
import com.sciencebot.pos.users.UserFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SaleServiceImplTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private SaleMapper saleMapper;

    @Mock
    private CustomerFacade customerFacade;

    @Mock
    private ProductFacade productFacade;

    @Mock
    private InventoryFacade inventoryFacade;

    @Mock
    private UserFacade userFacade;

    @Mock
    private com.sciencebot.pos.billing.BillingFacade billingFacade;

    @InjectMocks
    private SaleServiceImpl saleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerSale_Success() {
        CreateSaleItemCommand item = new CreateSaleItemCommand(1L, 2);
        CreateSaleCommand command = new CreateSaleCommand(1L, BigDecimal.valueOf(20.00), true, List.of(item));

        CustomerDto customer = new CustomerDto(1L, "Maria Lopez", "123", "a@a.com", "123", "Calle", true);
        when(customerFacade.getById(1L)).thenReturn(Optional.of(customer));

        ProductDto product = new ProductDto(1L, "P1", "123", "Prod 1", "Cat", BigDecimal.ONE, BigDecimal.valueOf(8.00), 10, 2, true, null);
        when(productFacade.getById(1L)).thenReturn(Optional.of(product));
        when(saleRepository.getNextInvoiceSeq()).thenReturn(1L);

        Sale saved = new Sale();
        saved.setId(100L);
        saved.setInvoiceNumber("FACT-000001");
        saved.setTotalAmount(BigDecimal.valueOf(16.00));
        saved.setCashReceived(BigDecimal.valueOf(20.00));
        saved.setCashChange(BigDecimal.valueOf(4.00));
        when(saleRepository.save(any(Sale.class))).thenReturn(saved);

        SaleDto expectedDto = new SaleDto(100L, "FACT-000001", null, "Maria Lopez", BigDecimal.valueOf(16.00), BigDecimal.valueOf(20.00), BigDecimal.valueOf(4.00), "admin", List.of());
        when(saleMapper.toDto(any(Sale.class))).thenReturn(expectedDto);

        SaleDto result = saleService.registerSale(command);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(16.00), result.totalAmount());
        assertEquals(BigDecimal.valueOf(4.00), result.cashChange());
        verify(inventoryFacade, times(1)).registerMovement(eq(1L), eq("VENTA"), eq(2), anyString());
        verify(saleRepository, times(1)).save(any(Sale.class));
    }

    @Test
    void registerSale_InsufficientCash_ThrowsException() {
        CreateSaleItemCommand item = new CreateSaleItemCommand(1L, 2);
        CreateSaleCommand command = new CreateSaleCommand(1L, BigDecimal.valueOf(10.00), false, List.of(item)); // Total is 16, cash is 10

        CustomerDto customer = new CustomerDto(1L, "Maria Lopez", "123", "a@a.com", "123", "Calle", true);
        when(customerFacade.getById(1L)).thenReturn(Optional.of(customer));

        ProductDto product = new ProductDto(1L, "P1", "123", "Prod 1", "Cat", BigDecimal.ONE, BigDecimal.valueOf(8.00), 10, 2, true, null);
        when(productFacade.getById(1L)).thenReturn(Optional.of(product));
        when(saleRepository.getNextInvoiceSeq()).thenReturn(1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
                () -> saleService.registerSale(command));
        assertTrue(ex.getMessage().contains("efectivo recibido"));
    }
}
