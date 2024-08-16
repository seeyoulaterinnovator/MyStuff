package ru.alamics.sso.jpa.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.keycloak.events.jpa.AdminEventEntity;
import org.keycloak.models.utils.KeycloakModelUtils;

@ApplicationScoped
public class AdminEventRepository {

    @Inject
    private EntityManager em;

    @Transactional(Transactional.TxType.REQUIRED)
    public AdminEventEntity save(AdminEventEntity adminEventEntity) {
        if (adminEventEntity.getId() == null) {
            adminEventEntity.setId(KeycloakModelUtils.generateId());
        }
        em.persist(adminEventEntity);
        em.flush();

        return adminEventEntity;
    }
}
