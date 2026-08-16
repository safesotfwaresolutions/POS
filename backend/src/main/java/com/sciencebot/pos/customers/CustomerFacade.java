package com.sciencebot.pos.customers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface CustomerFacade {
    Optional<CustomerDto> getById(Long id);
    Optional<CustomerDto> getByIdentification(String identification);
    CustomerDto createCustomer(CreateCustomerCommand command);
    CustomerDto updateCustomer(Long id, UpdateCustomerCommand command);
    void deleteCustomer(Long id);
    Page<CustomerDto> searchCustomers(String search, Pageable pageable);
}
