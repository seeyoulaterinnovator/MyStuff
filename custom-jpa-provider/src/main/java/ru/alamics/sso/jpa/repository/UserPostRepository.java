package ru.alamics.sso.jpa.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.jpa.entity.ExternalSystemEntity;
import ru.alamics.sso.jpa.entity.ExternalSystemRoleEntity;
import ru.alamics.sso.jpa.entity.UserPostEntity;
import ru.alamics.sso.jpa.entity.UserPostRoleEntity;
import ru.alamics.sso.jpa.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
@Slf4j
public class UserPostRepository {
    @Inject
    EntityManager em;

    @Transactional
    public UserPostEntity save(UserPostEntity userPost) {
        if (userPost.getId() == null || userPost.getId().isEmpty()) {
            userPost.setId(UUID.randomUUID().toString());
        }
        em.persist(userPost);
        em.flush();
        return userPost;
    }

    @Transactional
    public UserPostEntity update(UserPostEntity userPost) {
        em.merge(userPost);
        em.flush();
        return userPost;
    }

    @Transactional
    public void remove(UserPostEntity post) {
        em.remove(post);
    }

    public UserPostEntity getUserPost(String id) {
        return em.find(UserPostEntity.class, id);
    }

    public UserPostEntity findUserPostByUserIdAndTomsId(String userId, String tomsId) {
        List<UserPostEntity> result = em.createQuery(
                        "select ac " +
                                "from UserPostEntity ac " +
                                "where ac.customer.id = :toms_id " +
                                "and ac.user.id = :userId", UserPostEntity.class)
                .setParameter("userId", userId)
                .setParameter("toms_id", tomsId)
                .getResultList();
        return CollectionUtils.nullOrGet(result, 0);

    }

    public List<UserPostEntity> getAllUserPost() {
        return em.createQuery(
                "select ac from UserPostEntity ac " +
                        "left join fetch ac.user " +
                        "left join fetch ac.customer " +
                        "left join fetch ac.role " +
                        "left join fetch ac.brand",
                UserPostEntity.class
        ).getResultList();
    }

    public List<UserPostEntity> getAllUserPostByUserId(String userId) {
        return em.createQuery(
                "select upe from UserPostEntity upe " +
                        "left join fetch upe.user " +
                        "left join fetch upe.customer " +
                        "left join fetch upe.role " +
                        "left join fetch upe.brand " +
                        "where upe.user.id = :userId",
                UserPostEntity.class
        ).setParameter("userId", userId).getResultList();
    }

    public List<UserPostEntity> findUserPostsByUserIds(List<String> userIds) {
        return em.createQuery(
                "select distinct upe from UserPostEntity upe " +
                        "left join fetch upe.user " +
                        "left join fetch upe.systemRoles sr " +
                        "left join fetch upe.role " +
                        "left join fetch upe.customer " +
                        "left join fetch sr.externalSystem " +
                        "left join fetch upe.brand " +
                        "where upe.user.id in :userIds",
                UserPostEntity.class
        ).setParameter("userIds", userIds).getResultList();
    }

    public List<UserPostEntity> findUserPostsByIds(List<String> userPostIds) {
        if (userPostIds.isEmpty()) return Collections.emptyList();
        return em.createQuery(
                "select upe from UserPostEntity upe " +
                        "left join fetch upe.user " +
                        "left join fetch upe.systemRoles " +
                        "left join fetch upe.role " +
                        "left join fetch upe.customer " +
                        "left join fetch upe.brand " +
                        "where upe.id in :userPostIds",
                UserPostEntity.class
        ).setParameter("userPostIds", userPostIds).getResultList();
    }

    public List<UserPostRoleEntity> getAllUserPostRoles() {
        return em.createQuery("select apr from UserPostRoleEntity apr", UserPostRoleEntity.class)
                .getResultList();
    }

    public ExternalSystemRoleEntity getExternalSystemRole(String sysName, String realmId) {
        List<ExternalSystemRoleEntity> result = em.createQuery(
                        "select role " +
                                "from ExternalSystemRoleEntity role \n" +
                                "join ExternalSystemEntity sys on role.externalSystem = sys.id \n" +
                                "where sys.name = :sysName and sys.realmId = :realmId ", ExternalSystemRoleEntity.class)
                .setParameter("sysName", sysName)
                .setParameter("realmId", realmId)
                .getResultList();
        return CollectionUtils.nullOrGet(result, 0);
    }

    public UserPostRoleEntity getUserPostRole(String name) {
        List<UserPostRoleEntity> result = em.createQuery(
                        "select role " +
                                "from UserPostRoleEntity role \n" +
                                "where role.name = :name", UserPostRoleEntity.class)
                .setParameter("name", name)
                .getResultList();
        return CollectionUtils.nullOrGet(result, 0);
    }

    public List<UserPostEntity> findUserPostsByUser(final UserEntity user) {
        return em.createQuery(
                "select up from UserPostEntity up " +
                        "left join fetch up.customer " +
                        "left join fetch up.role " +
                        "left join fetch up.brand " +
                        "where up.user = :user",
                UserPostEntity.class
        ).setParameter("user", user).getResultList();
    }

    public List<UserPostEntity> findUserPostRoleByUserId(final String userId) throws NotFoundException {
        final String DEBUG_STR = "findUserPostRole";
        log.debug("{}: userId={}", DEBUG_STR, userId);

        try {
            return new ArrayList<>(findUserPostsByUser(em.find(UserEntity.class, userId)));
        } catch (NotFoundException e) {
            log.info("User not found by id={}", userId);
            throw new NotFoundException("User not found");
        }

    }

    public List<ExternalSystemEntity> getAllExternalSystemForRealm(String realmId) {
        return em.createQuery("select sys from ExternalSystemEntity sys where sys.realmId =:realm_id ", ExternalSystemEntity.class)
                .setParameter("realm_id", realmId)
                .getResultList();
    }

    public List<ExternalSystemRoleEntity> getAllExternalSystemRoleForRealm(String realmId) {
        return em.createQuery("select role from ExternalSystemRoleEntity role where role.realmId =:realm_id ", ExternalSystemRoleEntity.class)
                .setParameter("realm_id", realmId)
                .getResultList();
    }

    public UserPostRoleEntity findUserPostRoleById(Long id) {
        return em.find(UserPostRoleEntity.class, id);
    }

    public ExternalSystemRoleEntity findExternalSystemRole(Long id) {
        return em.find(ExternalSystemRoleEntity.class, id);
    }
}
