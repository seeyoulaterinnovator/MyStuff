package ru.alamics.sso.jpa.repository;

import org.keycloak.models.jpa.entities.ClientEntity;
import ru.alamics.sso.jpa.entity.MainRedirectUri;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;


@Stateless
@LocalBean
public class ClientRepository {

    @PersistenceContext
    private EntityManager em;

    public ClientEntity findClientById(final String clientId, final String realmId) {
        ClientEntity client = null;
        try {
            client = em.createQuery(
                    "select cl " +
                            "from ClientEntity cl " +
                            "where cl.clientId = :client_id and cl.realm.name = :realmId ", ClientEntity.class)
                    .setParameter("client_id", clientId)
                    .setParameter("realmId", realmId)
                    .getSingleResult();
        } finally {
            return client;
        }
    }

    public List<MainRedirectUri> findMainRedirectUrisByClientId(final String clientId) {
        List<MainRedirectUri> uris = em.createQuery(
                "select ru " +
                        "from MainRedirectUri ru " +
                        "where ru.clientId = :clientId ", MainRedirectUri.class)
                .setParameter("clientId", clientId)
                .getResultList();

        return uris;
    }

    public MainRedirectUri findMainRedirectUriByClientId(final String clientId) {
        return em.find(MainRedirectUri.class, clientId);
    }

    public void saveMainRedirectUri(MainRedirectUri uri) {
        MainRedirectUri mainRedirectUri = findMainRedirectUriByClientId(uri.getClientId());

        if (mainRedirectUri == null) {
            em.persist(uri);
        } else {
            mainRedirectUri.setUri(uri.getUri());
            em.merge(mainRedirectUri);
        }

        em.flush();
    }

}
