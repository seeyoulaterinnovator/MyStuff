package ru.alamics.sso.jpa.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import ru.alamics.sso.jpa.entity.AppProperty;

import java.util.List;

@ApplicationScoped
public class AppPropertyRepository {

    @Inject
    private EntityManager em;

    public List<AppProperty> findAll() {
        return em.createQuery("select ap from AppProperty ap ", AppProperty.class).getResultList();
    }

    public AppProperty findByName(String name) {
        return em.find(AppProperty.class, name);
    }
}
