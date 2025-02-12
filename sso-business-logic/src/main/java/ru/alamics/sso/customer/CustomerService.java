package ru.alamics.sso.customer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.jpa.entity.Customer;
import ru.alamics.sso.jpa.repository.CustomerRepository;

@ApplicationScoped
@Slf4j
public class CustomerService {
    @Inject
    CustomerRepository customerRepository;

    public CustomerDto findById(String tomsId) {
        return toCustomerDto(customerRepository.findByTomsId(tomsId));
    }

    public CustomerDto save(CustomerDto customer) {
        return toCustomerDto(customerRepository.save(toCustomer(customer)));
    }

    private Customer toCustomer(CustomerDto customer) {
        if (customer == null) {
            return null;
        }
        return Customer.builder()
                .id(customer.getTomsId())
                .name(customer.getName())
                .build();
    }

    private CustomerDto toCustomerDto(Customer customer) {
        if (customer == null) {
            return null;
        }
        return new CustomerDto(customer.getId(), customer.getName());
    }


}
