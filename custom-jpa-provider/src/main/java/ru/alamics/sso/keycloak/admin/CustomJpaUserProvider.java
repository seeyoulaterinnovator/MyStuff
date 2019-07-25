package ru.alamics.sso.keycloak.admin;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.JpaUserProvider;
import org.keycloak.models.jpa.UserAdapter;
import org.keycloak.models.jpa.entities.UserEntity;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class CustomJpaUserProvider extends JpaUserProvider {

    private KeycloakSession session;

    public CustomJpaUserProvider(KeycloakSession session, EntityManager em) {
        super(session, em);
        this.session = session;
    }

    @Override
    public List<UserModel> searchForUser(String search, RealmModel realm, int firstResult, int maxResults) {
        log.info("searchForUser");

        TypedQuery<UserEntity> query = em.createQuery(
                "select distinct u from UserEntity u " +
                        "join u.attributes attr " +
                        "where u.realmId = :realmId " +
                        "and (u.serviceAccountClientLink is null) " +
                        "and ( lower(u.username) like :search " +
                        "or lower(concat(u.firstName, ' ', u.lastName)) like :search " +
                        "or u.email like :search ) " +
                        "or attr.value like :search " +
                        "order by u.username",
                UserEntity.class);
        query.setParameter("realmId", realm.getId());
        query.setParameter("search", "%" + search.toLowerCase() + "%");
        if (firstResult != -1) {
            query.setFirstResult(firstResult);
        }
        if (maxResults != -1) {
            query.setMaxResults(maxResults);
        }
        List<UserEntity> results = query.getResultList();
        List<UserModel> users = new LinkedList<>();
        for (UserEntity entity : results) users.add(new UserAdapter(session, realm, em, entity));
        return users;

    }
}
