package ru.alamics.sso.jpa.admin;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.JpaUserProvider;
import org.keycloak.models.jpa.UserAdapter;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.models.utils.KeycloakModelUtils;
import ru.alamics.sso.jpa.entity.AutoLockNotification;
import ru.alamics.sso.jpa.entity.UserLoginHistory;
import ru.alamics.sso.jpa.entity.UserPostEntity;
import ru.alamics.sso.jpa.model.CustomUserAdapter;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class CustomJpaUserProvider extends JpaUserProvider {
    private final KeycloakSession session;

    public CustomJpaUserProvider(KeycloakSession session, EntityManager em) {
        super(session, em);
        this.session = session;
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, String search, Integer firstResult, Integer maxResults) {
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
        return query.getResultStream().map(entity -> new UserAdapter(session, realm, em, entity));
    }

    @Override
    public boolean removeUser(RealmModel realm, UserModel user) {
        log.info("remove user starts");
        UserEntity userEntity = em.find(UserEntity.class, user.getId());
        if (userEntity == null) return false;
        removeUser(userEntity);
        return true;
    }

    private void removeUser(UserEntity user) {
        String id = user.getId();
        em.createNativeQuery(AutoLockNotification.DELETE_BY_USER_SQL).setParameter("user", user).executeUpdate();
        em.createNativeQuery(UserLoginHistory.DELETE_BY_USER_SQL).setParameter("user", user).executeUpdate();
        em.createNamedQuery("deleteUserRoleMappingsByUser").setParameter("user", user).executeUpdate();
        em.createNamedQuery("deleteUserGroupMembershipsByUser").setParameter("user", user).executeUpdate();
        em.createNamedQuery("deleteFederatedIdentityByUser").setParameter("user", user).executeUpdate();
        em.createNamedQuery("deleteUserConsentClientScopesByUser").setParameter("user", user).executeUpdate();
        em.createNamedQuery("deleteUserConsentsByUser").setParameter("user", user).executeUpdate();

        //em.createNativeQuery("delete from USERPOST_EXT_SYSTEM_ROLE where USER_POST_ID in (select id from USER_POST where USER_ID =:user_id)")
        //        .setParameter("user_id", user.getId()).executeUpdate();

        removePostSystem(user);

        em.createNativeQuery(UserPostEntity.DELETE_BY_USER_SQL).setParameter("user", user).executeUpdate();

        em.flush();
        // not sure why i have to do a clear() here.  I was getting some messed up errors that Hibernate couldn't
        // un-delete the UserEntity.
        em.clear();
        user = em.find(UserEntity.class, id);
        if (user != null) {
            em.remove(user);
        }

        em.flush();
        log.info("remove user ends");
    }

    private void removePostSystem(UserEntity user) {

        List<String> ret = em.createQuery("select id from UserPostEntity e where e.user = :user_id", String.class)
                .setParameter("user_id", user)
                .getResultList();

        for (String postId : ret) {

            em.createNativeQuery("delete from USERPOST_EXT_SYSTEM_ROLE where USER_POST_ID = :post_id")
                    .setParameter("post_id", postId).executeUpdate();
        }
    }

    @Override
    public UserModel addUser(RealmModel realm, String id, String username, boolean addDefaultRoles, boolean addDefaultRequiredActions) {
        return super.addUser(realm, id, username, addDefaultRoles, addDefaultRequiredActions);
    }

    @Override
    public UserModel addUser(RealmModel realm, String username) {
        try {
            String adr = session.getContext().getUri().getAbsolutePath().toString();
            if (adr.contains("bss")) {
                return addUser(realm, KeycloakModelUtils.generateId(), username.toLowerCase(), true, false);
            }
        } catch (NullPointerException e) { // can be thrown from getUri if user added through ./add-user-keycloak.sh
            log.warn("Error while adding user, use super.addUser method", e);
        }

        return addUser(realm, KeycloakModelUtils.generateId(), username.toLowerCase(), true, true);
    }

    // версия 6.0.1 без проверки realm-а (необходима для менеджеров)
    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        UserEntity userEntity = this.em.find(UserEntity.class, id);
        if(userEntity == null) return null;
        return new CustomUserAdapter(
                this.session,
                session.getProvider(RealmProvider.class).getRealm(userEntity.getRealmId()),
                this.em,
                userEntity
        );
    }
}
