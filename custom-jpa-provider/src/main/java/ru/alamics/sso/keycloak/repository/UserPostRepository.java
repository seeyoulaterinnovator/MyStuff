package ru.alamics.sso.keycloak.repository;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.keycloak.entity.UserPost;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.UUID;

@Slf4j
@Stateless
@LocalBean
public class UserPostRepository {

    @PersistenceContext
    private EntityManager em;

    public UserPost save(UserPost userPost) {
        if (userPost.getId() == null ||  userPost.getId().isBlank()) {
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
        em.flush();
    }

    public UserPost getUserPost(String id) {
        UserPost access = em.find(UserPost.class, id);
        em.flush();
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
            em.flush();
            return access;
        }
    }
}
