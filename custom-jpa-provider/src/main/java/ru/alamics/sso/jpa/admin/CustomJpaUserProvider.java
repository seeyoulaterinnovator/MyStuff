package ru.alamics.sso.jpa.admin;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.JpaUserProvider;
import org.keycloak.models.jpa.UserAdapter;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.jpa.entity.UserLoginHistory;
import ru.alamics.sso.jpa.entity.AutoLockNotification;
import ru.alamics.sso.jpa.entity.UserPostEntity;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.*;

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
                        "or u.email like :search " +
                        "or attr.value like :search ) " +
                        "order by u.username",
                UserEntity.class);
        if (realm.getId().equals("manager"))
            query.setParameter("realmId", "user");
        else
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

    @Override
    public boolean removeUser (RealmModel realm, UserModel user) {
        UserEntity userEntity = em.find(UserEntity.class, user.getId());
        if (userEntity == null) return false;
        removeUser(userEntity);
        return true;
    }

    private void removeUser(UserEntity user) {
        String id = user.getId();
        em.createNativeQuery(AutoLockNotification.DELETE_BY_USER_SQL).setParameter("user", user).executeUpdate();
        em.createNativeQuery(UserPostEntity.DELETE_BY_USER_SQL).setParameter("user", user).executeUpdate();
        em.createNativeQuery(UserLoginHistory.DELETE_BY_USER_SQL).setParameter("user", user).executeUpdate();
        em.createNamedQuery("deleteUserRoleMappingsByUser").setParameter("user", user).executeUpdate();
        em.createNamedQuery("deleteUserGroupMembershipsByUser").setParameter("user", user).executeUpdate();
        em.createNamedQuery("deleteFederatedIdentityByUser").setParameter("user", user).executeUpdate();
        em.createNamedQuery("deleteUserConsentClientScopesByUser").setParameter("user", user).executeUpdate();
        em.createNamedQuery("deleteUserConsentsByUser").setParameter("user", user).executeUpdate();
        em.createNativeQuery("delete from USERPOST_EXT_SYSTEM_ROLE where USER_POST_ID in (select id from USER_POST where USER_ID =:user_id)")
                .setParameter("user_id", user.getId()).executeUpdate();
        em.flush();
        // not sure why i have to do a clear() here.  I was getting some messed up errors that Hibernate couldn't
        // un-delete the UserEntity.
        em.clear();
        user = em.find(UserEntity.class, id);
        if (user != null) {
            em.remove(user);
        }

        em.flush();
    }
}
