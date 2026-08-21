package com.sciencebot.pos.sales;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.Optional;

public interface SaleFacade {
    SaleDto registerSale(CreateSaleCommand command);

    /**
     * Registra una venta de forma idempotente. Si {@code idempotencyKey} no es nulo y ya
     * existe una venta con esa clave, devuelve la venta original sin crear un duplicado
     * ni descontar inventario de nuevo.
     */
    SaleDto registerSale(CreateSaleCommand command, String idempotencyKey);

    /** Recupera una venta por su clave de idempotencia (para resolver reenvios concurrentes). */
    Optional<SaleDto> findByIdempotencyKey(String idempotencyKey);

    Optional<SaleDto> getById(Long id);
    Page<SaleDto> searchSales(
            Long customerId,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Pageable pageable
    );
}
