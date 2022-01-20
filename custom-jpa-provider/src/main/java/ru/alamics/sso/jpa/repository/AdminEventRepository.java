package ru.alamics.sso.jpa.repository;

import org.keycloak.events.jpa.AdminEventEntity;
import org.keycloak.models.utils.KeycloakModelUtils;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@LocalBean
@Stateless
public class AdminEventRepository {

    @PersistenceContext
    private EntityManager em;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public AdminEventEntity save(AdminEventEntity adminEventEntity) {
        if (adminEventEntity.getId() == null) {
            adminEventEntity.setId(KeycloakModelUtils.generateId());
        }
        em.persist(adminEventEntity);
        em.flush();

        return adminEventEntity;
    }
}
