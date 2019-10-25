package ru.alamics.sso.keycloak.repository;

import org.keycloak.events.jpa.AdminEventEntity;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.models.utils.KeycloakModelUtils;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@LocalBean
@Stateless
public class AdminEventRepository {

    @PersistenceContext
    private EntityManager em;

    public AdminEventEntity save(AdminEventEntity adminEventEntity) {
        if (adminEventEntity.getId() == null) {
            adminEventEntity.setId(KeycloakModelUtils.generateId());
        }
        em.persist(adminEventEntity);
        em.flush();

        return adminEventEntity;
    }
}
