package com.sciencebot.pos.reports;

import com.sciencebot.pos.reports.internal.services.ReportServiceImpl;
import com.sciencebot.pos.products.ProductFacade;
import com.sciencebot.pos.suppliers.SupplierFacade;
import com.sciencebot.pos.users.UserFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReportServiceImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private ProductFacade productFacade;

    @Mock
    private SupplierFacade supplierFacade;

    @Mock
    private UserFacade userFacade;

    @InjectMocks
    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getSalesReport_Success() {
        TypedQuery mockQuery = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        
        Object[] totals = new Object[] { BigDecimal.valueOf(100.00), 5L };
        when(mockQuery.getSingleResult()).thenReturn(totals);
        when(mockQuery.getResultList()).thenReturn(new ArrayList<>());

        SalesReportDto result = reportService.getSalesReport(LocalDateTime.now().minusDays(5), LocalDateTime.now());

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(100.00), result.totalSold());
        assertEquals(5L, result.transactionCount());
    }
}
