package ru.alamics.sso.jpa.repository;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.jpa.entity.AppProperty;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ValidationException;
import java.util.List;

@Stateless
@LocalBean
@Slf4j
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


    public boolean checkStatusDb() {
        try {
            //em.createNativeQuery("select 1").getSingleResult();
            em.getTransaction().commit();
        } catch (Exception e) {
            log.error("checkStatusDb", e);
            return false;
        }

        return true;
    }
}
