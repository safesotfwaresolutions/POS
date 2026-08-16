package com.sciencebot.pos.reports.internal.services;

import com.sciencebot.pos.reports.*;
import com.sciencebot.pos.products.ProductFacade;
import com.sciencebot.pos.products.ProductDto;
import com.sciencebot.pos.suppliers.SupplierFacade;
import com.sciencebot.pos.users.UserFacade;
import com.sciencebot.pos.users.UserDto;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final EntityManager entityManager;
    private final ProductFacade productFacade;
    private final SupplierFacade supplierFacade;
    private final UserFacade userFacade;

    public ReportServiceImpl(
            EntityManager entityManager,
            @Lazy ProductFacade productFacade,
            @Lazy SupplierFacade supplierFacade,
            @Lazy UserFacade userFacade
    ) {
        this.entityManager = entityManager;
        this.productFacade = productFacade;
        this.supplierFacade = supplierFacade;
        this.userFacade = userFacade;
    }

    @Override
    public SalesReportDto getSalesReport(LocalDateTime dateFrom, LocalDateTime dateTo) {
        LocalDateTime from = (dateFrom != null) ? dateFrom : LocalDateTime.now().minusDays(30);
        LocalDateTime to = (dateTo != null) ? dateTo : LocalDateTime.now();

        // 1. Total sold & Transaction count
        Object[] totals = (Object[]) entityManager.createQuery(
                "SELECT COALESCE(SUM(s.totalAmount), 0), COUNT(s) FROM Sale s WHERE s.createdAt >= :dateFrom AND s.createdAt <= :dateTo")
                .setParameter("dateFrom", from)
                .setParameter("dateTo", to)
                .getSingleResult();

        BigDecimal totalSold = (BigDecimal) totals[0];
        long transactionCount = (long) totals[1];

        // 2. Sales by seller
        List<Object[]> salesBySellerRaw = entityManager.createQuery(
                "SELECT s.userId, COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.createdAt >= :dateFrom AND s.createdAt <= :dateTo GROUP BY s.userId")
                .setParameter("dateFrom", from)
                .setParameter("dateTo", to)
                .getResultList();

        Map<String, BigDecimal> salesBySeller = new HashMap<>();
        for (Object[] row : salesBySellerRaw) {
            Long userId = (Long) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            String username = "Desconocido";
            try {
                username = userFacade.getById(userId).username();
            } catch (Exception e) {
                // Fallback
            }
            salesBySeller.put(username, amount);
        }

        return new SalesReportDto(from, to, totalSold, transactionCount, salesBySeller);
    }

    @Override
    public List<TopProductDto> getTopProductsReport(LocalDateTime dateFrom, LocalDateTime dateTo, int limit) {
        LocalDateTime from = (dateFrom != null) ? dateFrom : LocalDateTime.now().minusDays(30);
        LocalDateTime to = (dateTo != null) ? dateTo : LocalDateTime.now();
        int maxResults = (limit > 0) ? limit : 10;

        List<Object[]> raw = entityManager.createQuery(
                "SELECT i.productId, COALESCE(SUM(i.quantity), 0) FROM SaleItem i JOIN i.sale s WHERE s.createdAt >= :dateFrom AND s.createdAt <= :dateTo GROUP BY i.productId ORDER BY SUM(i.quantity) DESC")
                .setParameter("dateFrom", from)
                .setParameter("dateTo", to)
                .setMaxResults(maxResults)
                .getResultList();

        List<TopProductDto> list = new ArrayList<>();
        for (Object[] row : raw) {
            Long productId = (Long) row[0];
            long quantity = (Long) row[1];
            String productName = productFacade.getById(productId)
                    .map(p -> p.name())
                    .orElse("Producto Desconocido");
            list.add(new TopProductDto(productId, productName, quantity));
        }

        return list;
    }

    @Override
    public List<StockReportDto> getStockReport(Boolean belowMinStock) {
        List<Object[]> raw;
        if (Boolean.TRUE.equals(belowMinStock)) {
            raw = entityManager.createQuery(
                    "SELECT p.id, p.name, p.quantityAvailable, p.minStock FROM Product p WHERE p.quantityAvailable <= p.minStock AND p.active = true")
                    .getResultList();
        } else {
            raw = entityManager.createQuery(
                    "SELECT p.id, p.name, p.quantityAvailable, p.minStock FROM Product p WHERE p.active = true")
                    .getResultList();
        }

        List<StockReportDto> list = new ArrayList<>();
        for (Object[] row : raw) {
            Long id = (Long) row[0];
            String name = (String) row[1];
            int stock = (int) row[2];
            int minStock = (int) row[3];
            String status = (stock <= minStock) ? "Bajo Stock" : "OK";
            list.add(new StockReportDto(id, name, stock, minStock, status));
        }

        return list;
    }

    @Override
    public List<ProfitabilityReportDto> getProfitabilityReport(LocalDateTime dateFrom, LocalDateTime dateTo) {
        LocalDateTime from = (dateFrom != null) ? dateFrom : LocalDateTime.now().minusDays(30);
        LocalDateTime to = (dateTo != null) ? dateTo : LocalDateTime.now();

        List<Object[]> raw = entityManager.createQuery(
                "SELECT i.productId, COALESCE(SUM(i.quantity), 0) FROM SaleItem i JOIN i.sale s WHERE s.createdAt >= :dateFrom AND s.createdAt <= :dateTo GROUP BY i.productId")
                .setParameter("dateFrom", from)
                .setParameter("dateTo", to)
                .getResultList();

        List<ProfitabilityReportDto> list = new ArrayList<>();
        for (Object[] row : raw) {
            Long productId = (Long) row[0];
            long quantity = (Long) row[1];
            Optional<ProductDto> productOpt = productFacade.getById(productId);
            if (productOpt.isPresent()) {
                ProductDto product = productOpt.get();
                BigDecimal profit = product.salePrice()
                        .subtract(product.purchasePrice())
                        .multiply(BigDecimal.valueOf(quantity));
                list.add(new ProfitabilityReportDto(productId, product.name(), profit));
            }
        }

        return list;
    }

    @Override
    public PurchasesReportDto getPurchasesReport(LocalDateTime dateFrom, LocalDateTime dateTo) {
        LocalDateTime from = (dateFrom != null) ? dateFrom : LocalDateTime.now().minusDays(30);
        LocalDateTime to = (dateTo != null) ? dateTo : LocalDateTime.now();

        // 1. Total inverted
        BigDecimal totalInverted = (BigDecimal) entityManager.createQuery(
                "SELECT COALESCE(SUM(p.totalAmount), 0) FROM Purchase p WHERE p.createdAt >= :dateFrom AND p.createdAt <= :dateTo")
                .setParameter("dateFrom", from)
                .setParameter("dateTo", to)
                .getSingleResult();

        // 2. Purchases by supplier
        List<Object[]> raw = entityManager.createQuery(
                "SELECT p.supplierId, COALESCE(SUM(p.totalAmount), 0) FROM Purchase p WHERE p.createdAt >= :dateFrom AND p.createdAt <= :dateTo GROUP BY p.supplierId")
                .setParameter("dateFrom", from)
                .setParameter("dateTo", to)
                .getResultList();

        Map<String, BigDecimal> purchasesBySupplier = new HashMap<>();
        for (Object[] row : raw) {
            Long supplierId = (Long) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            String supplierName = supplierFacade.getById(supplierId)
                    .map(s -> s.companyName())
                    .orElse("Proveedor Desconocido");
            purchasesBySupplier.put(supplierName, amount);
        }

        return new PurchasesReportDto(from, to, totalInverted, purchasesBySupplier);
    }
}
