package ru.alamics.sso.keycloak.repository;

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
import java.util.List;
import java.util.UUID;

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

    public List<UserPostRoleEntity> getAllUserPostRoles(){
        return em.createQuery(
                "select apr " +
                        "from UserPostRoleEntity apr", UserPostRoleEntity.class)
                .getResultList();
    }

    public List<UserPostEntity> findUserPostRole(final UserEntity user) {
        final String DEBUG_STR = "findUserPostRole";
        log.info("{}: user={}", DEBUG_STR, user.getId());

        List<UserPostEntity> ret = em.createQuery("select up from UserPostEntity up where up.user =:user", UserPostEntity.class)
                .setParameter("user", user)
                .getResultList();
        return ret;
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

    public UserPostRoleEntity findUserPostRole(Long id){
        return em.find(UserPostRoleEntity.class, id);
    }

    public ExternalSystemRoleEntity findExternalSystemRole(Long id){
        return em.find(ExternalSystemRoleEntity.class, id);
    }
}
