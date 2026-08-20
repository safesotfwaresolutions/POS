package com.sciencebot.pos.customers.internal.services;

import com.sciencebot.pos.customers.*;
import com.sciencebot.pos.customers.internal.entities.Customer;
import com.sciencebot.pos.customers.internal.repositories.CustomerRepository;
import com.sciencebot.pos.customers.internal.mappers.CustomerMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerFacade {

    private static final String GENERAL_CUSTOMER_IDENTIFICATION = "9999999999";

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerServiceImpl(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    @Override
    public Optional<CustomerDto> getById(Long id) {
        return customerRepository.findById(id).map(customerMapper::toDto);
    }

    @Override
    public Optional<CustomerDto> getByIdentification(String identification) {
        return customerRepository.findByIdentification(identification).map(customerMapper::toDto);
    }

    @Override
    @Transactional
    public CustomerDto createCustomer(CreateCustomerCommand command) {
        if (command.fullName() == null || command.fullName().isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio");
        }
        if (command.identification() == null || command.identification().isBlank()) {
            throw new IllegalArgumentException("La identificación del cliente es obligatoria");
        }
        
        String cleanIdentification = command.identification().trim();
        if (customerRepository.existsByIdentification(cleanIdentification)) {
            throw new IllegalArgumentException("Ya existe un cliente con la identificación: " + cleanIdentification);
        }

        Customer customer = new Customer();
        customer.setFullName(command.fullName().trim());
        customer.setIdentification(cleanIdentification);
        customer.setEmail(command.email() != null ? command.email().trim() : null);
        customer.setPhone(command.phone() != null ? command.phone().trim() : null);
        customer.setAddress(command.address() != null ? command.address().trim() : null);
        customer.setActive(true);

        Customer saved = customerRepository.save(customer);
        return customerMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CustomerDto updateCustomer(Long id, UpdateCustomerCommand command) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con ID: " + id));

        checkNotGeneralCustomer(customer);

        if (command.fullName() == null || command.fullName().isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio");
        }

        customer.setFullName(command.fullName().trim());
        customer.setEmail(command.email() != null ? command.email().trim() : null);
        customer.setPhone(command.phone() != null ? command.phone().trim() : null);
        customer.setAddress(command.address() != null ? command.address().trim() : null);

        Customer saved = customerRepository.save(customer);
        return customerMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con ID: " + id));

        checkNotGeneralCustomer(customer);

        customer.setActive(false);
        customerRepository.save(customer);
    }

    @Override
    public Page<CustomerDto> searchCustomers(String search, Pageable pageable) {
        String cleanSearch = (search == null || search.isBlank()) ? null : search.trim();
        if (cleanSearch == null) {
            return customerRepository.findAll(pageable).map(customerMapper::toDto);
        }
        return customerRepository.searchCustomers(cleanSearch, pageable).map(customerMapper::toDto);
    }

    private void checkNotGeneralCustomer(Customer customer) {
        if (GENERAL_CUSTOMER_IDENTIFICATION.equals(customer.getIdentification())) {
            throw new IllegalArgumentException("No se puede editar ni eliminar el Cliente General");
        }
    }
}
