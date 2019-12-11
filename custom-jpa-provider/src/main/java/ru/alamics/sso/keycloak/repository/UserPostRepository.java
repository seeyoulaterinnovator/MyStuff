package ru.alamics.sso.keycloak.repository;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.keycloak.entity.ExternalSystemEntity;
import ru.alamics.sso.keycloak.entity.ExternalSystemRoleEntity;
import ru.alamics.sso.keycloak.entity.UserPostEntity;
import ru.alamics.sso.keycloak.entity.UserPostRoleEntity;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Stateless
@LocalBean
public class UserPostRepository {

    @PersistenceContext
    private EntityManager em;

    public UserPostEntity save(UserPostEntity userPost) {
        if (userPost.getId() == null || userPost.getId().isBlank()) {
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

    public UserPostEntity getUserPost(String userId, String tomsId) {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        UserPostEntity access = null;
        try {
            access = em.createQuery(
                    "select ac " +
                            "from UserPostEntity ac " +
                            "where ac.tomsId = :toms_id and ac.user = :user", UserPostEntity.class)
                    .setParameter("user", userEntity)
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

    public List<UserPostRoleEntity> getAllUserPostRoles(){
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
        final String DEBUG_STR = "findUserPostRole";
        log.info("{}: user={}", DEBUG_STR, user.getId());

        List<UserPostEntity> ret = em.createQuery("select up from UserPostEntity up where up.user =:user", UserPostEntity.class)
                .setParameter("user", user)
                .getResultList();
        return ret;
    }

    public List<UserPostEntity> findUserPostRoleByUserId(final String userId) throws NotFoundException {
        final String DEBUG_STR = "findUserPostRole";
        log.info("{}: userId={}", DEBUG_STR, userId);

        UserEntity userEntity = em.find(UserEntity.class, userId);
        if (userEntity != null) {
            return new ArrayList<>(findUserPostsByUser(userEntity));
        } else {
            log.info("User not found by id={}", userId);
            throw new NotFoundException("User not found");
        }
    }

    public List<ExternalSystemEntity> getAllExternalSystem(){
        return em.createQuery(
                "select sys " +
                        "from ExternalSystemEntity sys", ExternalSystemEntity.class)
                .getResultList();
    }

    public List<ExternalSystemRoleEntity> getAllExternalSystemRole(){
        return em.createQuery(
                "select role " +
                        "from ExternalSystemRoleEntity role", ExternalSystemRoleEntity.class)
                .getResultList();
    }

    public UserPostRoleEntity findUserPostsByUser(Long id){
        return em.find(UserPostRoleEntity.class, id);
    }

    public ExternalSystemRoleEntity findExternalSystemRole(Long id){
        return em.find(ExternalSystemRoleEntity.class, id);
    }

    public UserPostEntity findUserPostByParam(String userId, final String tomsId, final String roleName) {

        List<UserPostEntity> ret = em.createQuery("select upe from UserPostEntity upe where upe.tomsId =:toms and upe.role.name =:role and upe.user.id = :user_id ", UserPostEntity.class)
                .setParameter("toms", tomsId)
                .setParameter("role", roleName)
                .setParameter("user_id", userId)
                .getResultList();

        return Optional.of(ret.get(0)).orElseThrow(() -> new IllegalArgumentException("Cannot find user post with"));
    }

    public List<UserPostEntity> findUserPostByToms(String userId, final String tomsId) {

        List<UserPostEntity> ret = em.createQuery("select upe from UserPostEntity upe where upe.tomsId =:toms and upe.user.id = :user_id ", UserPostEntity.class)
                .setParameter("toms", tomsId)
                .setParameter("user_id", userId)
                .getResultList();

        return ret;
    }

    public List<ExternalSystemRoleEntity> findSystemsByUserPost(final UserPostEntity post) {

        List<ExternalSystemRoleEntity> ret = em.createQuery("select ext from ExternalSystemRoleEntity ext left join ext.userPosts post where post.id =:postId", ExternalSystemRoleEntity.class)
                .setParameter("postId", post.getId())
                .getResultList();

        return ret;
    }

    public List<ExternalSystemRoleEntity> findSystemByUser(final UserEntity user) {

        List<ExternalSystemRoleEntity> ret = em.createQuery("select ext from ExternalSystemRoleEntity ext join ext.userPosts post where post.user =:user", ExternalSystemRoleEntity.class)
                .setParameter("user", user)
                .getResultList();

        return ret;
    }
}
