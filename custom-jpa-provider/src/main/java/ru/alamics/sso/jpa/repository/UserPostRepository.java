package ru.alamics.sso.jpa.repository;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.jpa.entity.ExternalSystemEntity;
import ru.alamics.sso.jpa.entity.ExternalSystemRoleEntity;
import ru.alamics.sso.jpa.entity.UserPostRoleEntity;
import ru.alamics.sso.jpa.entity.UserPostEntity;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

@Slf4j
@Stateless
@LocalBean
public class UserPostRepository {

    @PersistenceContext
    private EntityManager em;

    public UserPostEntity save(UserPostEntity userPost) {
        if (userPost.getId() == null || userPost.getId().isEmpty()) {
            userPost.setId(UUID.randomUUID().toString());
        }
        em.persist(userPost);
        em.flush();
        return userPost;
    }

    public UserPostEntity update(UserPostEntity userPost) {
        em.merge(userPost);
        em.flush();
        return userPost;
    }

    public void remove(String id) {
        UserPostEntity userPost = em.find(UserPostEntity.class, id);
        em.remove(userPost);
//        em.flush();
    }

    public UserPostEntity getUserPost(String id) {
        UserPostEntity access = em.find(UserPostEntity.class, id);
        return access;
    }

    public UserPostEntity findUserPostByUserIdAndTomsId(String userId, String tomsId) {
        UserPostEntity access = null;
        try {
            access = em.createQuery(
                    "select ac " +
                            "from UserPostEntity ac " +
                            "where ac.customer.id = :toms_id " +
                            "and ac.user.id = :userId", UserPostEntity.class)
                    .setParameter("userId", userId)
                    .setParameter("toms_id", tomsId)
                    .getSingleResult();
        } finally {
            return access;
        }
    }

    public List<UserPostEntity> getAllUserPost() {
        return em.createQuery(
                "select ac " +
                        "from UserPostEntity ac", UserPostEntity.class)
                .getResultList();
    }

    public List<UserPostEntity> getAllUserPostByUserId(String userId) {
        return em.createQuery(
                "select upe " +
                        "from UserPostEntity upe " +
                        "where upe.user.id = :userId ", UserPostEntity.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public List<UserPostEntity> findUserPostsByUserIds(List<String> userIds) {
        return em.createQuery(
                "select distinct upe " +
                        "from UserPostEntity upe " +
                        "left join fetch upe.user " +
                        "left join fetch upe.systemRoles sr " +
                        "left join fetch upe.role " +
                        "left join fetch upe.customer " +
                        "left join fetch sr.externalSystem " +
                        "where upe.user.id in :userIds ", UserPostEntity.class)
                .setParameter("userIds", userIds)
                .getResultList();
    }

    public List<UserPostEntity> findUserPostsByIds(List<String> userPostIds) {
        if (userPostIds.isEmpty()) {
            return Collections.emptyList();
        }
        return em.createQuery(
                "select upe " +
                        "from UserPostEntity upe " +
                        "left join fetch upe.user " +
                        "left join fetch upe.systemRoles " +
                        "left join fetch upe.role " +
                        "left join fetch upe.customer " +
                        "where upe.id in :userPostIds ", UserPostEntity.class)
                .setParameter("userPostIds", userPostIds)
                .getResultList();
    }

    public List<UserPostRoleEntity> getAllUserPostRoles() {
        return em.createQuery(
                "select apr " +
                        "from UserPostRoleEntity apr", UserPostRoleEntity.class)
                .getResultList();
    }

    public ExternalSystemRoleEntity getExternalSystemRole(String sysName) {
        ExternalSystemRoleEntity externalSystemRole = null;
        try {
            externalSystemRole = em.createQuery(
                    "select role " +
                            "from ExternalSystemRoleEntity role \n" +
                            "join ExternalSystemEntity sys on role.externalSystem = sys.id \n" +
                            "where sys.name = :sysName", ExternalSystemRoleEntity.class)
                    .setParameter("sysName", sysName)
                    .getResultList()
                    .get(0);
        } finally {
            return externalSystemRole;
        }
    }

    public UserPostRoleEntity getUserPostRole(String name) {
        UserPostRoleEntity userPostRoleEntity = null;
        try {
            userPostRoleEntity = em.createQuery(
                    "select role " +
                            "from UserPostRoleEntity role \n" +
                            "where role.name = :name", UserPostRoleEntity.class)
                    .setParameter("name", name)
                    .getResultList()
                    .get(0);
        } finally {
            return userPostRoleEntity;
        }
    }

    public List<UserPostEntity> findUserPostsByUser(final UserEntity user) {
        final String DEBUG_STR = "findUserPosts";
        log.debug("{}: user={}", DEBUG_STR, user.getId());

        List<UserPostEntity> ret = em.createQuery("select up from UserPostEntity up where up.user =:user", UserPostEntity.class)
                .setParameter("user", user)
                .getResultList();
        return ret;
    }

    public List<UserPostEntity> findUserPostRoleByUserId(final String userId) throws NotFoundException {
        final String DEBUG_STR = "findUserPostRole";
        log.debug("{}: userId={}", DEBUG_STR, userId);

        UserEntity userEntity = em.find(UserEntity.class, userId);
        if (userEntity != null) {
            return new ArrayList<>(findUserPostsByUser(userEntity));
        } else {
            log.info("User not found by id={}", userId);
            throw new NotFoundException("User not found");
        }
    }

    public List<ExternalSystemEntity> getAllExternalSystem() {
        return em.createQuery(
                "select sys " +
                        "from ExternalSystemEntity sys", ExternalSystemEntity.class)
                .getResultList();
    }

    public List<ExternalSystemRoleEntity> getAllExternalSystemRole() {
        return em.createQuery(
                "select role " +
                        "from ExternalSystemRoleEntity role", ExternalSystemRoleEntity.class)
                .getResultList();
    }

    public UserPostRoleEntity findUserPostRoleById(Long id) {
        return em.find(UserPostRoleEntity.class, id);
    }

    public ExternalSystemRoleEntity findExternalSystemRole(Long id) {
        return em.find(ExternalSystemRoleEntity.class, id);
    }

    public UserPostEntity findUserPostByParam(String userId, final String tomsId, final String roleName) {

        List<UserPostEntity> ret = em.createQuery("select upe from UserPostEntity upe where upe.customer.id =:toms and upe.role.name =:role and upe.user.id = :user_id ", UserPostEntity.class)
                .setParameter("toms", tomsId)
                .setParameter("role", roleName)
                .setParameter("user_id", userId)
                .getResultList();

        return Optional.of(ret.get(0)).orElseThrow(() -> new IllegalArgumentException("Cannot find user post with"));
    }
}
