package ru.alamics.sso.keycloak.repository;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.keycloak.entity.ExternalSystem;
import ru.alamics.sso.keycloak.entity.ExternalSystemRole;
import ru.alamics.sso.keycloak.entity.UserPost;
import ru.alamics.sso.keycloak.entity.UserPostRole;

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

    public UserPost save(UserPost userPost) {
        if (userPost.getId() == null || userPost.getId().isBlank()) {
            userPost.setId(UUID.randomUUID().toString());
        }
        em.persist(userPost);
        em.flush();
        return userPost;
    }

    public UserPost update(UserPost userPost) {
        em.merge(userPost);
        em.flush();
        return userPost;
    }

    public void remove(String id) {
        UserPost userPost = em.find(UserPost.class, id);
        em.remove(userPost);
//        em.flush();
    }

    public UserPost getUserPost(String id) {
        UserPost access = em.find(UserPost.class, id);
        return access;
    }

    public UserPost getUserPost(String userId, String tomsId) {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        UserPost access = null;
        try {
            access = em.createQuery(
                    "select ac " +
                            "from UserPost ac " +
                            "where ac.tomsId = :toms_id and ac.user = :user", UserPost.class)
                    .setParameter("user", userEntity)
                    .setParameter("toms_id", tomsId)
                    .getSingleResult();
        } finally {
//            em.flush();
            return access;
        }
    }

    public List<UserPost> getAllUserPost() {
        return em.createQuery(
                "select ac " +
                        "from UserPost ac", UserPost.class)
                .getResultList();
    }

    public List<UserPostRole> getAllUserPostRoles(){
        return em.createQuery(
                "select apr " +
                        "from UserPostRole apr", UserPostRole.class)
                .getResultList();
    }

    public List<UserPost> findUserPostRole(final UserEntity user) {
        final String DEBUG_STR = "findUserPostRole";
        log.info("{}: user={}", DEBUG_STR, user.getId());

        List<UserPost> ret = em.createQuery("select up from UserPost up where up.user =:user", UserPost.class)
                .setParameter("user", user)
                .getResultList();
        return ret;
    }

    public List<ExternalSystem> getAllExternalSystem(){
        return em.createQuery(
                "select sys " +
                        "from ExternalSystem sys", ExternalSystem.class)
                .getResultList();
    }

    public List<ExternalSystemRole> getAllExternalSystemRole(){
        return em.createQuery(
                "select role " +
                        "from ExternalSystemRole role", ExternalSystemRole.class)
                .getResultList();
    }

    public UserPost addSystemRole(UserPost userPost, Long extSystemRoleId){
        userPost.getSystemRoles().add(em.find(ExternalSystemRole.class, extSystemRoleId));
        update(userPost);
        return userPost;
    }
}
