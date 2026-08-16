package com.sciencebot.pos.billing.internal.repositories;

import com.sciencebot.pos.billing.internal.entities.ElectronicInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ElectronicInvoiceRepository extends JpaRepository<ElectronicInvoice, Long> {
    Optional<ElectronicInvoice> findBySaleId(Long saleId);
    List<ElectronicInvoice> findByStatus(String status);
}
