package ru.alamics.sso.jpa.repository;

import org.keycloak.models.jpa.entities.ClientEntity;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


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
}
