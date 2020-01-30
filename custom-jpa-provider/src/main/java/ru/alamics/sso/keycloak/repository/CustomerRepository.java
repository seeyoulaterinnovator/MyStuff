package ru.alamics.sso.keycloak.repository;

import ru.alamics.sso.keycloak.entity.Customer;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@LocalBean
@Stateless
public class CustomerRepository {

    @PersistenceContext
    private EntityManager em;

    public Customer save(Customer customer) {
        Customer customerInDb = findByTomsId(customer.getId());
        if (customerInDb == null) {
            em.persist(customer);
        } else {
            if (customerInDb.getName() != null && (customer.getName() == null || customer.getName().isBlank())) {
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
