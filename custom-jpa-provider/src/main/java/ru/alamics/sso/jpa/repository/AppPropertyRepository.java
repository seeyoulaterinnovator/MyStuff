package ru.alamics.sso.jpa.repository;

import ru.alamics.sso.jpa.entity.AppProperty;

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
        if (appProperty.getName() == null || appProperty.getName().isEmpty()) {
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


    public int checkStatusDb() {
        try {
            em.createNativeQuery("select 1").getSingleResult();
        } catch (Exception e) {
            return 0;
        }

        return 1;
    }
}
