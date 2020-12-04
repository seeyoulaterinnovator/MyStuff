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
}
