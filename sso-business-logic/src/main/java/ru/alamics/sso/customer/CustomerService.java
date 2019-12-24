package ru.alamics.sso.customer;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.keycloak.entity.Customer;
import ru.alamics.sso.keycloak.repository.CustomerRepository;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Stateless
public class CustomerService {

    @EJB
    private CustomerRepository customerRepository;

    public CustomerDto save(CustomerDto customer) {
        return toCustomerDto(customerRepository.save(toCustomer(customer)));
    }

    public List<CustomerDto> findAll() {
        return customerRepository.findAll().stream().map(this::toCustomerDto).collect(Collectors.toList());
    }

    private Customer toCustomer(CustomerDto customer) {
        if (customer == null) {
            return null;
        }
        return null;//new Customer(customer.getTomsId(), customer.getName());
    }

    private CustomerDto toCustomerDto(Customer customer) {
        if (customer == null) {
            return null;
        }
        return new CustomerDto(customer.getId(), customer.getName());
    }


}
