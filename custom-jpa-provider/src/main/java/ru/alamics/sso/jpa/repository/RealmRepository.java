package ru.alamics.sso.jpa.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.keycloak.models.RealmModel;
import org.keycloak.models.jpa.RealmAdapter;
import org.keycloak.models.jpa.entities.RealmEntity;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class RealmRepository {
    @Inject
    private EntityManager em;

    public RealmModel findRealmById(final String id) {
        RealmEntity realm = em.createQuery("select r from RealmEntity r " +
                "left join fetch r.smtpConfig " +
                "where r.id = :id ", RealmEntity.class)
                .setParameter("id", id)
                .getSingleResult();
        if (realm == null) return null;
        RealmAdapter adapter = new RealmAdapter(null, em, realm);
        return adapter;
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
}
