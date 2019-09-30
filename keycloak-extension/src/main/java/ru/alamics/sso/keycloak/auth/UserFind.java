package ru.alamics.sso.keycloak.auth;

import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.UserAdapter;
import org.keycloak.models.jpa.entities.UserEntity;


import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

import static ru.alamics.sso.registration.model.UserConstants.ATTR_PHONE_NAME;

public class UserFind {

    private KeycloakSession session;
    private RealmModel realmModel;
    private EntityManager em;

    public UserFind (KeycloakSession session) {
        this.session = session;
        var context = this.session.getContext();
        this.realmModel = context.getRealm();
        this.em = this.session.getProvider(JpaConnectionProvider.class).getEntityManager();
    }

    public UserModel getUserByPhone(String str) {
        TypedQuery<UserEntity> query = em.createQuery(

                "select u from UserEntity u " +
                        "join UserAttributeEntity ua on u.id = ua.user " +
                        "where ua.name = :ph_attr_name and ua.value like '%' || :phone || '%'" // TODO =
                , UserEntity.class)
                .setParameter("ph_attr_name", ATTR_PHONE_NAME)
                .setParameter("phone", str);
        List<UserEntity> results = query.getResultList();
        if (results.isEmpty()) return null;
        return new UserAdapter(session, session.getContext().getRealm(), em, results.get(0));
    }
}
