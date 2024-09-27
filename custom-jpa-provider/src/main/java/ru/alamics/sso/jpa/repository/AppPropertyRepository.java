package ru.alamics.sso.jpa.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import ru.alamics.sso.jpa.entity.AppProperty;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AppPropertyRepository {
    @Inject
    EntityManager em;

    @Transactional
    public List<AppProperty> findAll() {
        return em.createQuery("select ap from AppProperty ap ", AppProperty.class).getResultList();
    }

    @Transactional
    public Optional<AppProperty> findByName(String name) {
        return em.createQuery("select ap from AppProperty ap where ap.name = :name", AppProperty.class)
                .setParameter("name", name)
                .getResultList()
                .stream()
                .findFirst();
    }
}
