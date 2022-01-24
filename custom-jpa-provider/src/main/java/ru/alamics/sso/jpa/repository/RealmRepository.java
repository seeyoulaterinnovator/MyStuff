package ru.alamics.sso.jpa.repository;

import org.keycloak.models.RealmModel;
import org.keycloak.models.jpa.RealmAdapter;
import org.keycloak.models.jpa.entities.RealmEntity;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;


@Stateless
@LocalBean
public class RealmRepository {

    @PersistenceContext
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

    public List<RealmModel> getAllRealm() {
        List<RealmEntity> realmsEntities = em.createQuery("select realm from RealmEntity realm", RealmEntity.class)
                .getResultList();

        List<RealmModel> realmModels = new ArrayList<>();

        for (RealmEntity entity : realmsEntities) {
            RealmAdapter adapter = new RealmAdapter(null, em, entity);
            realmModels.add(adapter);
        }

        return realmModels;
    }

    public RealmEntity findRealmEntityById(final String id) {
        return em.find(RealmEntity.class, id);
    }
}
