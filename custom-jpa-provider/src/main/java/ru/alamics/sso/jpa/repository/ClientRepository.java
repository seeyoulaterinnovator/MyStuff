package ru.alamics.sso.jpa.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.keycloak.models.jpa.entities.ClientEntity;
import ru.alamics.sso.jpa.entity.MainRedirectUri;

@ApplicationScoped
public class ClientRepository {
    @Inject
    EntityManager em;

    public ClientEntity findClientByIdAndRealmName(final String clientId, final String realmName) {
        return em.createQuery(
                        "select cl " +
                                "from ClientEntity cl " +
                                "where cl.clientId = :clientId " +
                                    "and cl.realmId = (select r.id from RealmEntity r where r.name = :realmName)",
                        ClientEntity.class
                )
                .setParameter("clientId", clientId)
                .setParameter("realmName", realmName)
                .getSingleResult();
    }

    public MainRedirectUri findMainRedirectUriByClientId(final String clientId) {
        return em.find(MainRedirectUri.class, clientId);
    }

    @Transactional
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
