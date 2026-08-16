package com.sciencebot.pos.customers.internal.repositories;

import com.sciencebot.pos.customers.internal.entities.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Optional<Customer> findByIdentification(String identification);
    
    boolean existsByIdentification(String identification);
    
    @Query("SELECT c FROM Customer c WHERE " +
           "(:search IS NULL OR " +
           "LOWER(c.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.identification) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Customer> searchCustomers(@Param("search") String search, Pageable pageable);
}
