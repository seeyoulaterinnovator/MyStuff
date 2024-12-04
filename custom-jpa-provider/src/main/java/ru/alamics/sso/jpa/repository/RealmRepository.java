package ru.alamics.sso.jpa.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.keycloak.models.RealmModel;
import org.keycloak.models.jpa.RealmAdapter;
import org.keycloak.models.jpa.entities.RealmEntity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class RealmRepository {
    @Inject
    EntityManager em;

    public RealmModel findRealmById(final String id) {
        RealmEntity realm = em.createQuery("select r from RealmEntity r " +
                "left join fetch r.smtpConfig " +
                "where r.id = :id ", RealmEntity.class)
                .setParameter("id", id)
                .getResultList()
                .stream()
                .findFirst()
                .orElse(null);
        if (realm == null) return null;
        return new RealmAdapter(null, em, realm);
    }

    public List<RealmModel> getAllRealms() {
        return em.createQuery("select realm from RealmEntity realm", RealmEntity.class)
                .getResultList().stream()
                .map(realmEntity -> new RealmAdapter(null, em, realmEntity))
                .collect(Collectors.toList());

    }

    public RealmEntity findRealmEntityById(final String id) {
        return em.find(RealmEntity.class, id);
    }

    public Optional<RealmEntity> findRealmEntityByName(final String name) {
        return em.createQuery("select r from RealmEntity r " +
                        "where r.name = :name ", RealmEntity.class)
                .setParameter("name", name)
                .getResultList()
                .stream()
                .findFirst();
    }
}
