package ru.alamics.sso.keycloak.repository;

import ru.alamics.sso.keycloak.entity.AppProperty;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ValidationException;
import java.util.List;

@Stateless
@LocalBean
public class AppPropertyRepository {

    @PersistenceContext
    private EntityManager em;

    public List<AppProperty> findAll() {
        return em.createQuery("select ap from AppProperty ap ", AppProperty.class).getResultList();
    }

    public AppProperty findByName(String name) {
        return em.find(AppProperty.class, name);
    }

    public AppProperty save(AppProperty appProperty) {
        if (appProperty.getName() == null || appProperty.getName().isBlank()) {
            throw new ValidationException("Name is required!");
        }
        AppProperty result = findByName(appProperty.getName());
        if (result == null) {
            em.persist(appProperty);
        } else {
            em.merge(appProperty);
        }
        em.flush();
        return appProperty;
    }
}
