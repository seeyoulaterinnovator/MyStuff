package ru.alamics.sso.keycloak.repository;

import lombok.extern.slf4j.Slf4j;
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
        if (userPost.getId().isBlank()){
            userPost.setId(UUID.randomUUID().toString());
        }
        em.persist(userPost);
        em.flush();
        return userPost;
    }

    public UserPost update(UserPost access) {
        if (access.getId().isBlank()){
            access.setId(UUID.randomUUID().toString());
        }
        em.merge(access);
        em.flush();
        return access;
    }

    public void remove(UserPost access) {
        em.remove(access);
        em.flush();
    }

    public UserPost getUserPost(String id) {
        UserPost access = em.find(UserPost.class, id);
        em.flush();
        return access;
    }

    public UserPost getUserPost(String userId, String tomsId) {
        UserPost access = em.createQuery(
                "select ac " +
                        "from UserPost ac " +
                        "where ac.toms_id = :toms_id and ac.user_id = :user_id", UserPost.class)
                .setParameter("user_id", userId)
                .setParameter("toms_id", tomsId)
                .getSingleResult();
        em.flush();
        return access;
    }
}
