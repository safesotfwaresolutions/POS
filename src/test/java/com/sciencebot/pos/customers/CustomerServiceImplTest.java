package com.sciencebot.pos.customers;

import com.sciencebot.pos.customers.internal.entities.Customer;
import com.sciencebot.pos.customers.internal.repositories.CustomerRepository;
import com.sciencebot.pos.customers.internal.services.CustomerServiceImpl;
import com.sciencebot.pos.customers.internal.mappers.CustomerMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createCustomer_Success() {
        CreateCustomerCommand command = new CreateCustomerCommand("Maria Lopez", "1234567890", "maria@mail.com", "555-0100", "Calle 1");
        when(customerRepository.existsByIdentification("1234567890")).thenReturn(false);

        Customer saved = new Customer();
        saved.setId(2L);
        saved.setFullName("Maria Lopez");
        saved.setIdentification("1234567890");
        when(customerRepository.save(any(Customer.class))).thenReturn(saved);

        CustomerDto expectedDto = new CustomerDto(2L, "Maria Lopez", "1234567890", "maria@mail.com", "555-0100", "Calle 1", true);
        when(customerMapper.toDto(any(Customer.class))).thenReturn(expectedDto);

        CustomerDto result = customerService.createCustomer(command);

        assertNotNull(result);
        assertEquals(2L, result.id());
        assertEquals("1234567890", result.identification());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void updateCustomer_GeneralCustomer_ThrowsException() {
        Customer general = new Customer();
        general.setId(1L);
        general.setFullName("Cliente General");
        general.setIdentification("9999999999");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(general));

        UpdateCustomerCommand command = new UpdateCustomerCommand("Nuevo Nombre", "a@a.com", "123", "Calle 2");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> customerService.updateCustomer(1L, command));
        assertTrue(ex.getMessage().contains("Cliente General"));
    }

    @Test
    void deleteCustomer_GeneralCustomer_ThrowsException() {
        Customer general = new Customer();
        general.setId(1L);
        general.setFullName("Cliente General");
        general.setIdentification("9999999999");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(general));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> customerService.deleteCustomer(1L));
        assertTrue(ex.getMessage().contains("Cliente General"));
    }
}
