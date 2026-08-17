package com.sciencebot.pos.reports.internal.services;

import com.sciencebot.pos.reports.*;
import com.sciencebot.pos.reports.internal.repositories.ReportDao;
import com.sciencebot.pos.reports.internal.repositories.ReportProjections.SalesTotalsProjection;
import com.sciencebot.pos.reports.internal.repositories.ReportProjections.SellerSaleProjection;
import com.sciencebot.pos.reports.internal.repositories.ReportProjections.SupplierPurchaseProjection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private static final int DEFAULT_REPORT_DAYS = 30;
    private static final int DEFAULT_TOP_PRODUCTS_LIMIT = 10;

    private final ReportDao reportDao;

    public ReportServiceImpl(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    @Override
    public SalesReportDto getSalesReport(LocalDateTime dateFrom, LocalDateTime dateTo) {
        LocalDateTime from = (dateFrom != null) ? dateFrom : LocalDateTime.now().minusDays(DEFAULT_REPORT_DAYS);
        LocalDateTime to = (dateTo != null) ? dateTo : LocalDateTime.now();

        SalesTotalsProjection totals = reportDao.findSalesTotals(from, to);
        List<SellerSaleProjection> sellerSales = reportDao.findSalesBySeller(from, to);

        Map<String, BigDecimal> salesBySeller = new LinkedHashMap<>();
        for (SellerSaleProjection sale : sellerSales) {
            salesBySeller.put(sale.sellerName(), sale.totalAmount());
        }

        return new SalesReportDto(from, to, totals.totalSold(), totals.transactionCount(), salesBySeller);
    }

    @Override
    public List<TopProductDto> getTopProductsReport(LocalDateTime dateFrom, LocalDateTime dateTo, int limit) {
        LocalDateTime from = (dateFrom != null) ? dateFrom : LocalDateTime.now().minusDays(DEFAULT_REPORT_DAYS);
        LocalDateTime to = (dateTo != null) ? dateTo : LocalDateTime.now();
        int maxResults = (limit > 0) ? limit : DEFAULT_TOP_PRODUCTS_LIMIT;

        return reportDao.findTopSellingProducts(from, to, maxResults);
    }

    @Override
    public List<StockReportDto> getStockReport(Boolean belowMinStock) {
        return reportDao.findStockReport(belowMinStock);
    }

    @Override
    public List<ProfitabilityReportDto> getProfitabilityReport(LocalDateTime dateFrom, LocalDateTime dateTo) {
        LocalDateTime from = (dateFrom != null) ? dateFrom : LocalDateTime.now().minusDays(DEFAULT_REPORT_DAYS);
        LocalDateTime to = (dateTo != null) ? dateTo : LocalDateTime.now();

        return reportDao.findProfitabilityReport(from, to);
    }

    @Override
    public PurchasesReportDto getPurchasesReport(LocalDateTime dateFrom, LocalDateTime dateTo) {
        LocalDateTime from = (dateFrom != null) ? dateFrom : LocalDateTime.now().minusDays(DEFAULT_REPORT_DAYS);
        LocalDateTime to = (dateTo != null) ? dateTo : LocalDateTime.now();

        BigDecimal totalInverted = reportDao.findPurchasesTotal(from, to);
        List<SupplierPurchaseProjection> supplierPurchases = reportDao.findPurchasesBySupplier(from, to);

        Map<String, BigDecimal> purchasesBySupplier = new LinkedHashMap<>();
        for (SupplierPurchaseProjection purchase : supplierPurchases) {
            purchasesBySupplier.put(purchase.supplierName(), purchase.totalAmount());
        }

        return new PurchasesReportDto(from, to, totalInverted, purchasesBySupplier);
    }
}
