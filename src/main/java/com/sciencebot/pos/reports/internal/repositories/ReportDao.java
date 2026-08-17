package com.sciencebot.pos.reports.internal.repositories;

import com.sciencebot.pos.reports.ProfitabilityReportDto;
import com.sciencebot.pos.reports.StockReportDto;
import com.sciencebot.pos.reports.TopProductDto;
import com.sciencebot.pos.reports.internal.repositories.ReportProjections.SalesTotalsProjection;
import com.sciencebot.pos.reports.internal.repositories.ReportProjections.SellerSaleProjection;
import com.sciencebot.pos.reports.internal.repositories.ReportProjections.SupplierPurchaseProjection;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ReportDao {
    SalesTotalsProjection findSalesTotals(LocalDateTime from, LocalDateTime to);
    List<SellerSaleProjection> findSalesBySeller(LocalDateTime from, LocalDateTime to);
    List<TopProductDto> findTopSellingProducts(LocalDateTime from, LocalDateTime to, int limit);
    List<StockReportDto> findStockReport(Boolean belowMinStock);
    List<ProfitabilityReportDto> findProfitabilityReport(LocalDateTime from, LocalDateTime to);
    BigDecimal findPurchasesTotal(LocalDateTime from, LocalDateTime to);
    List<SupplierPurchaseProjection> findPurchasesBySupplier(LocalDateTime from, LocalDateTime to);
}
