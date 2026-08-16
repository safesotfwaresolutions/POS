package com.sciencebot.pos.reports;

import com.sciencebot.pos.reports.internal.repositories.ReportDao;
import com.sciencebot.pos.reports.internal.repositories.ReportProjections.SalesTotalsProjection;
import com.sciencebot.pos.reports.internal.repositories.ReportProjections.SellerSaleProjection;
import com.sciencebot.pos.reports.internal.repositories.ReportProjections.SupplierPurchaseProjection;
import com.sciencebot.pos.reports.internal.services.ReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReportServiceImplTest {

    @Mock
    private ReportDao reportDao;

    @InjectMocks
    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getSalesReport_Success() {
        when(reportDao.findSalesTotals(any(), any()))
                .thenReturn(new SalesTotalsProjection(BigDecimal.valueOf(100.00), 5L));
        when(reportDao.findSalesBySeller(any(), any()))
                .thenReturn(List.of(new SellerSaleProjection("cajero1", BigDecimal.valueOf(100.00))));

        SalesReportDto result = reportService.getSalesReport(LocalDateTime.now().minusDays(5), LocalDateTime.now());

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(100.00), result.totalSold());
        assertEquals(5L, result.transactionCount());
        assertTrue(result.salesBySeller().containsKey("cajero1"));
        assertEquals(BigDecimal.valueOf(100.00), result.salesBySeller().get("cajero1"));
    }

    @Test
    void getTopProductsReport_Success() {
        List<TopProductDto> mockList = List.of(
                new TopProductDto(1L, "Arroz", 50L),
                new TopProductDto(2L, "Frijol", 30L)
        );
        when(reportDao.findTopSellingProducts(any(), any(), eq(10)))
                .thenReturn(mockList);

        List<TopProductDto> result = reportService.getTopProductsReport(null, null, 10);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Arroz", result.get(0).productName());
    }

    @Test
    void getStockReport_Success() {
        List<StockReportDto> mockList = List.of(
                new StockReportDto(1L, "Aceite", 2, 5, "Bajo Stock")
        );
        when(reportDao.findStockReport(true)).thenReturn(mockList);

        List<StockReportDto> result = reportService.getStockReport(true);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Bajo Stock", result.get(0).status());
    }

    @Test
    void getProfitabilityReport_Success() {
        List<ProfitabilityReportDto> mockList = List.of(
                new ProfitabilityReportDto(1L, "Arroz", BigDecimal.valueOf(50000.00))
        );
        when(reportDao.findProfitabilityReport(any(), any())).thenReturn(mockList);

        List<ProfitabilityReportDto> result = reportService.getProfitabilityReport(null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(BigDecimal.valueOf(50000.00), result.get(0).profitAmount());
    }

    @Test
    void getPurchasesReport_Success() {
        when(reportDao.findPurchasesTotal(any(), any())).thenReturn(BigDecimal.valueOf(250000.00));
        when(reportDao.findPurchasesBySupplier(any(), any())).thenReturn(List.of(
                new SupplierPurchaseProjection("Distribuidora ABC", BigDecimal.valueOf(250000.00))
        ));

        PurchasesReportDto result = reportService.getPurchasesReport(null, null);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(250000.00), result.totalInverted());
        assertEquals(BigDecimal.valueOf(250000.00), result.purchasesBySupplier().get("Distribuidora ABC"));
    }
}
