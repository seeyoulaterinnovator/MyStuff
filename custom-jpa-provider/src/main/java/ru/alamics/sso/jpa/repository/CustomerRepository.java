package ru.alamics.sso.jpa.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import ru.alamics.sso.jpa.entity.Customer;

import java.util.List;

@ApplicationScoped
public class CustomerRepository {
    @Inject
    private EntityManager em;

    public Customer save(Customer customer) {
        Customer customerInDb = findByTomsId(customer.getId());
        if (customerInDb == null) {
            em.persist(customer);
        } else {
            if (customerInDb.getName() != null && (customer.getName() == null || customer.getName().isEmpty())) {
                customer.setName(customerInDb.getName());
            }
            customer = em.merge(customer);
        }
        em.flush();
        return customer;
    }

    public List<Customer> findAll() {
        return em.createQuery("select c from Customer c ", Customer.class).getResultList();
    }

    public Customer findByTomsId(String tomsId) {
        return em.find(Customer.class, tomsId);
    }
}
