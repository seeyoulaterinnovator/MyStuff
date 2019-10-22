package ru.alamics.sso.keycloak.repository;

import org.keycloak.models.RealmModel;
import org.keycloak.models.jpa.entities.UserAttributeEntity;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.services.validation.Validation;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import java.util.List;

@LocalBean
@Stateless
public class UserAttributeRepository {

    @PersistenceContext
    private EntityManager em;

    public UserAttributeEntity save(UserAttributeEntity attr) {
        if (attr.getId() == null) {
            attr.setId(KeycloakModelUtils.generateId());
            em.persist(attr);
        } else {
            em.merge(attr);
        }
        em.flush();
        return attr;
    }
}
