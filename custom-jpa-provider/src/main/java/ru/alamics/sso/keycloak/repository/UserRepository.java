package ru.alamics.sso.keycloak.repository;

import org.keycloak.models.RealmModel;
import org.keycloak.models.jpa.entities.UserAttributeEntity;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.services.validation.Validation;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.Tuple;
import java.util.List;

@LocalBean
@Stateless
public class UserRepository {

    private final static String SORT_FIELD_NAME = "firstName";
    private final static String SORT_FIELD_EMAIL = "email";

    @PersistenceContext
    private EntityManager em;

    public UserEntity findUser(final String userId) {
        UserEntity ret = em.find(UserEntity.class, userId);

        return ret;
    }

    public List<UserEntity> save(List<UserEntity> entities) {
        entities.forEach(entity -> {
            if (entity.getId() == null) {
                entity.setId(KeycloakModelUtils.generateId());
                em.persist(entity);
            } else {
                em.merge(entity);
            }
        });
        em.flush();

        return entities;
    }

    public UserEntity save(UserEntity user) {
        if (user.getId() == null) {
            user.setId(KeycloakModelUtils.generateId());
            em.persist(user);
        } else {
            em.merge(user);
        }
        em.flush();
        return user;
    }

    public UserAttributeEntity saveAttributes(UserAttributeEntity attr) {
        if (attr.getId() == null) {
            attr.setId(KeycloakModelUtils.generateId());
            em.persist(attr);
        } else {
            em.merge(attr);
        }
        em.flush();
        return attr;
    }

    public UserEntity getFirstUserByPhoneNumber(RealmModel realmModel, String phone, String excludedUserId) {

        if (Validation.isBlank(phone))
            return null;

        var users = em.createQuery("select u from UserEntity u join u.attributes attr \n" +
                "  where u.realmId = :realmId " +
                "       and attr.name = :name " +
                "       and (:excludedUserId is null or u.id <> :excludedUserId) " +
                "       and attr.value = :phoneNmbr", UserEntity.class)
                .setParameter("realmId", realmModel == null ? "user" : realmModel.getId())
                .setParameter("name", "phone")
                .setParameter("phoneNmbr", phone)
                .setParameter("excludedUserId", Validation.isBlank(excludedUserId) ? null : excludedUserId)
                .getResultList();
        if (users != null && users.size() > 0) {
            return users.get(0);
        }
        return null;
    }

    public UserEntity getFirstUserByPhoneNumber(String phone, String excludedUserId) {

        if (Validation.isBlank(phone))
            return null;

        var users = em.createQuery("select u from UserEntity u join u.attributes attr \n" +
                "  where attr.name = :name " +
                "       and (:excludedUserId is null or u.id <> :excludedUserId) " +
                "       and attr.value = :phoneNmbr", UserEntity.class)
                .setParameter("name", "phone")
                .setParameter("phoneNmbr", phone)
                .setParameter("excludedUserId", Validation.isBlank(excludedUserId) ? null : excludedUserId)
                .getResultList();
        if (users != null && users.size() > 0) {
            return users.get(0);
        }
        return null;
    }

    public UserEntity getFirstUserByPhone(String phone) {
        var users = em.createQuery(
                "select u from UserEntity u " +
                        "join u.attributes attr \n" +
                        "  where attr.name = :name " +
                        "       and attr.value = :phoneNmbr", UserEntity.class)
                .setParameter("name", "phone")
                .setParameter("phoneNmbr", phone)
                .getResultList();
        if (users != null && users.size() > 0) {
            return users.get(0);
        }
        return null;
    }

    public UserEntity getFirstUserByEmail(String realmId, String email) {
        var users = em.createQuery("select u from UserEntity u \n" +
                "  where u.realmId = :realmId and u.email = :email ", UserEntity.class)
                .setParameter("realmId", realmId)
                .setParameter("email", email)
                .getResultList();
        if (users != null && users.size() > 0) {
            return users.get(0);
        }
        return null;
    }

    public UserEntity getFirstUserByUsername(String realmId, String username) {
        var users = em.createQuery("select u from UserEntity u \n" +
                "  where u.realmId = :realmId and u.username = :username ", UserEntity.class)
                .setParameter("realmId", realmId)
                .setParameter("username", username)
                .getResultList();
        if (users != null && users.size() > 0) {
            return users.get(0);
        }
        return null;
    }


    public List<Tuple> getTupleUsersByParametersWithoutGrouping(String realm, String search, String searchUser, String searchToms, String sortField, boolean sortAsc) {
        Query query = em.createNativeQuery(
                "select " +
                        "       UE.ID         as user_id,\n" +
                        "       UE.USERNAME   as username,\n" +
                        "       UE.FIRST_NAME as first_name,\n" +
                        "       UE.LAST_NAME  as last_name,\n" +
                        "       UE.EMAIL      as email,\n" +
                        "       UA.VALUE      as phone,\n" +
                        "       UE.ENABLED    as enabled,\n" +
                        "       UP.id         as user_post_id,\n" +
                        "       UP.TOMS_ID    as toms_id,\n" +
                        "       UP.DMP_ID     as dmp_id,\n" +
                        "       UP.ROLE_ID    as role_id,\n" +
                        "       UPR.NAME      as role_name,\n" +
                        "       ESR.ID        as system_role_id,\n" +
                        "       ESR.NAME      as system_role,\n" +
                        "       ES.ID         as system_id,\n" +
                        "       ES.NAME       as system_name,\n" +
                        "       ES.LABEL      as system_label\n" +
                        "from USER_ENTITY UE\n" +
                        "         left join USER_ATTRIBUTE UA on UE.ID = UA.USER_ID and UA.NAME = 'phone'\n" +
                        "         left join USER_POST UP on UE.ID = UP.USER_ID\n" +
                        "         left join USER_POST_ROLE UPR on UP.ROLE_ID = UPR.ID\n" +
                        "         left join USERPOST_EXT_SYSTEM_ROLE UESR on UP.ID = UESR.USER_POST_ID\n" +
                        "         left join EXT_SYSTEM_ROLE ESR on UESR.EXT_SYSTEM_ROLE_ID = ESR.ID\n" +
                        "         left join EXTERNAL_SYSTEM ES on ESR.SYSTEM_ID = ES.ID\n" +
                        "WHERE UE.REALM_ID = :realm\n" +
                        "  AND CASE\n" +
                        "          WHEN :search is not null and :search != '' then (\n" +
                        "                      UE.EMAIL LIKE CONCAT('%', :search, '%') OR\n" +
                        "                      UE.FIRST_NAME LIKE CONCAT('%', :search, '%') OR\n" +
                        "                      UE.LAST_NAME LIKE CONCAT('%', :search, '%') OR\n" +
                        "                      UE.USERNAME LIKE CONCAT('%', :search, '%') OR\n" +
                        "                      UA.VALUE LIKE CONCAT('%', :search, '%')\n" +
                        "              )\n" +
                        "          else UE.ID LIKE '%' end\n" +
                        "  AND CASE\n" +
                        "          WHEN :searchUser is not null and :searchUser != '' then (UE.ID = :searchUser)\n" +
                        "          else UE.ID LIKE '%' OR  UE.ID is null end\n" +
                        "  AND CASE\n" +
                        "          WHEN :searchToms is not null and :searchToms != '' then (UP.TOMS_ID = :searchToms)\n" +
                        "          else UP.TOMS_ID LIKE '%' OR UP.TOMS_ID is null end\n" +
                        getSort(sortField, sortAsc), Tuple.class)
                .setParameter("search", search)
                .setParameter("searchUser", searchUser)
                .setParameter("searchToms", searchToms)
                .setParameter("realm", realm);

        return query.getResultList();
    }

    public long getTotalUsersByParameters(String realm, String search, String searchUser, String searchToms) {
        Query query = em.createNativeQuery(
                "select count(*) over()  " +
                        "from USER_ENTITY UE\n" +
                        "         left join USER_ATTRIBUTE UA on UE.ID = UA.USER_ID and UA.NAME = 'phone'\n" +
                        "         left join USER_POST UP on UE.ID = UP.USER_ID\n" +
                        "         left join USER_POST_ROLE UPR on UP.ROLE_ID = UPR.ID\n" +
                        "         left join USERPOST_EXT_SYSTEM_ROLE UESR on UP.ID = UESR.USER_POST_ID\n" +
                        "         left join EXT_SYSTEM_ROLE ESR on UESR.EXT_SYSTEM_ROLE_ID = ESR.ID\n" +
                        "         left join EXTERNAL_SYSTEM ES on ESR.SYSTEM_ID = ES.ID\n" +
                        "WHERE UE.REALM_ID = :realm\n" +
                        "  AND CASE\n" +
                        "          WHEN :search is not null and :search != '' then (\n" +
                        "                      UE.EMAIL LIKE CONCAT('%', :search, '%') OR\n" +
                        "                      UE.FIRST_NAME LIKE CONCAT('%', :search, '%') OR\n" +
                        "                      UE.LAST_NAME LIKE CONCAT('%', :search, '%') OR\n" +
                        "                      UE.USERNAME LIKE CONCAT('%', :search, '%') OR\n" +
                        "                      UA.VALUE LIKE CONCAT('%', :search, '%')\n" +
                        "              )\n" +
                        "          else UE.ID LIKE '%' end\n" +
                        "  AND CASE\n" +
                        "          WHEN :searchUser is not null and :searchUser != '' then (UE.ID = :searchUser)\n" +
                        "          else UE.ID LIKE '%' OR  UE.ID is null end\n" +
                        "  AND CASE\n" +
                        "          WHEN :searchToms is not null and :searchToms != '' then (UP.TOMS_ID = :searchToms)\n" +
                        "          else UP.TOMS_ID LIKE '%' OR UP.TOMS_ID is null end\n" +
                        "group by UE.ID ")
                .setParameter("search", search)
                .setParameter("searchUser", searchUser)
                .setParameter("searchToms", searchToms)
                .setParameter("realm", realm);

        return Long.parseLong(query.getSingleResult().toString());
    }

    public List<Tuple> getTupleUsersByParameters(String realm, String search, String searchUser, String searchToms, String sortField, boolean sortAsc,
                                                  Integer pageNum, Integer pageSize) {
        Query query = em.createNativeQuery(
                "select " +
                        "       UE.ID         as user_id,\n" +
                        "       UE.USERNAME   as username,\n" +
                        "       UE.FIRST_NAME as first_name,\n" +
                        "       UE.LAST_NAME  as last_name,\n" +
                        "       UE.EMAIL      as email,\n" +
                        "       UA.VALUE      as phone,\n" +
                        "       UE.ENABLED    as enabled,\n" +
                        "       group_concat(UP.id)  as user_post_ids\n" +
                        "from USER_ENTITY UE\n" +
                        "         left join USER_ATTRIBUTE UA on UE.ID = UA.USER_ID and UA.NAME = 'phone'\n" +
                        "         left join USER_POST UP on UE.ID = UP.USER_ID\n" +
                        "         left join USER_POST_ROLE UPR on UP.ROLE_ID = UPR.ID\n" +
                        "         left join USERPOST_EXT_SYSTEM_ROLE UESR on UP.ID = UESR.USER_POST_ID\n" +
                        "         left join EXT_SYSTEM_ROLE ESR on UESR.EXT_SYSTEM_ROLE_ID = ESR.ID\n" +
                        "         left join EXTERNAL_SYSTEM ES on ESR.SYSTEM_ID = ES.ID\n" +
                        "WHERE UE.REALM_ID = :realm\n" +
                        "  AND CASE\n" +
                        "          WHEN :search is not null and :search != '' then (\n" +
                        "                      UE.EMAIL LIKE CONCAT('%', :search, '%') OR\n" +
                        "                      UE.FIRST_NAME LIKE CONCAT('%', :search, '%') OR\n" +
                        "                      UE.LAST_NAME LIKE CONCAT('%', :search, '%') OR\n" +
                        "                      UE.USERNAME LIKE CONCAT('%', :search, '%') OR\n" +
                        "                      UA.VALUE LIKE CONCAT('%', :search, '%')\n" +
                        "              )\n" +
                        "          else UE.ID LIKE '%' end\n" +
                        "  AND CASE\n" +
                        "          WHEN :searchUser is not null and :searchUser != '' then (UE.ID = :searchUser)\n" +
                        "          else UE.ID LIKE '%' OR  UE.ID is null end\n" +
                        "  AND CASE\n" +
                        "          WHEN :searchToms is not null and :searchToms != '' then (UP.TOMS_ID = :searchToms)\n" +
                        "          else UP.TOMS_ID LIKE '%' OR UP.TOMS_ID is null end\n" +
                        "group by UE.ID " +
                        getSort(sortField, sortAsc), Tuple.class)
                .setParameter("search", search)
                .setParameter("searchUser", searchUser)
                .setParameter("searchToms", searchToms)
                .setParameter("realm", realm);

        if (pageNum != null && pageNum != 0 && pageSize != null && pageSize != 0) {
            query.setFirstResult((pageNum - 1) * pageSize);
            query.setMaxResults(pageSize);
        }

        return query.getResultList();
    }

    private String getSort(String sortField, boolean sortAsc) {
        String sort = "";
        if (SORT_FIELD_NAME.equalsIgnoreCase(sortField)) {
            sort += "ORDER BY first_name";
        } else if (SORT_FIELD_EMAIL.equalsIgnoreCase(sortField)) {
            sort += "ORDER BY email";
        }
        if (sort.isBlank()) {
            return sort;
        }
        if (!sortAsc) {
            sort += " DESC";
        }
        return sort;
    }
}