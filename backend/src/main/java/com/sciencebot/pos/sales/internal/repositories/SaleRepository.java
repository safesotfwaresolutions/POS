package com.sciencebot.pos.sales.internal.repositories;

import com.sciencebot.pos.sales.internal.entities.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    @Query(value = "SELECT nextval('invoice_seq')", nativeQuery = true)
    Long getNextInvoiceSeq();

    @Query("SELECT s FROM Sale s WHERE " +
           "(:customerId IS NULL OR s.customerId = :customerId) AND " +
           "(cast(:dateFrom as timestamp) IS NULL OR s.createdAt >= :dateFrom) AND " +
           "(cast(:dateTo as timestamp) IS NULL OR s.createdAt <= :dateTo)")
    Page<Sale> searchSales(
            @Param("customerId") Long customerId,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable);
}
