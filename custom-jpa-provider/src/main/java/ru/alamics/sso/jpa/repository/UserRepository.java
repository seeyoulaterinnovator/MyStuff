package ru.alamics.sso.jpa.repository;

import org.keycloak.models.RealmModel;
import org.keycloak.models.jpa.entities.UserAttributeEntity;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.services.validation.Validation;
import ru.alamics.sso.jpa.model.UserSummaryView;

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

        String realmName = realmModel == null ? "user" : realmModel.getId();

        // запрос в таком виде выполняется больше секунды на более 100т юзерах
        // с нормальным индексом уже без разницы на 6кк - 60мс, rows 1
        /*
        List<UserEntity> users1 = em.createQuery("select u from UserEntity u join u.attributes attr \n" +
                "  where u.realmId = :realmId " +
                "       and attr.name = :name " +
                "       and (:excludedUserId is null or u.id <> :excludedUserId) " +
                "       and attr.value = :phoneNmbr", UserEntity.class)
                .setParameter("realmId", realmModel == null ? "user" : realmModel.getId())
                .setParameter("name", "phone")
                .setParameter("phoneNmbr", phone)
                .setParameter("excludedUserId", Validation.isBlank(excludedUserId) ? null : excludedUserId)
                .getResultList();
        */

        List<UserEntity> users = (List<UserEntity>)em.createNativeQuery(
                "select * " +
                        "  from USER_ENTITY ue " +
                        "  where " +
                        " ue.REALM_ID = :realmId and " +
                        "     (:excludedUserId is null or ue.ID <> :excludedUserId) " +
                        "    and exists ( " +
                        "      select 1 " +
                        "      from USER_ATTRIBUTE attr " +
                        "      where attr.USER_ID = ue.ID " +
                        "        and attr.NAME = :name " +
                        "        and attr.VALUE = :phoneNmbr " +
                        "    )" +
                        "  limit 1"
                , UserEntity.class)
                .setParameter("realmId", realmName)
                .setParameter("name", "phone")
                .setParameter("phoneNmbr", phone)
                .setParameter("excludedUserId", Validation.isBlank(excludedUserId) ? null : excludedUserId)
                .getResultList();

        if (users != null && !users.isEmpty())
            return users.get(0);
        /*
        if (users != null && users.size() > 0) {

            for (UserEntity ue : users) {
                if (ue.getRealmId().equalsIgnoreCase(realmName))
                    return ue;
            }
        }
        */
        return null;
    }

    // TODO медленно, используется с правкой атрибутов
    public UserEntity getFirstUserByPhoneNumber(String phone, String excludedUserId) {

        if (Validation.isBlank(phone))
            return null;

        List<UserEntity> users = em.createQuery("select u from UserEntity u " +
                "join u.attributes attr " +
                "  where attr.name = :name " +
                "       and (:excludedUserId is null or u.id <> :excludedUserId) " +
                "       and attr.value = :phoneNmbr", UserEntity.class)
                .setParameter("name", "phone")
                .setParameter("phoneNmbr", phone)
                .setParameter("excludedUserId", Validation.isBlank(excludedUserId) ? null : excludedUserId)
                .setMaxResults(1)
                .getResultList();
        if (users != null && users.size() > 0) {
            return users.get(0);
        }
        return null;
    }

    // TODO медленно, используется при импорте
    public UserEntity getFirstUserByPhone(String phone) {
        List<UserEntity> users = em.createQuery(
                "select u from UserEntity u " +
                        "join u.attributes attr \n" +
                        "  where attr.name = :name " +
                        "       and attr.value = :phoneNmbr", UserEntity.class)
                .setParameter("name", "phone")
                .setParameter("phoneNmbr", phone)
                .setMaxResults(1)
                .getResultList();
        if (users != null && users.size() > 0) {
            return users.get(0);
        }
        return null;
    }

    public UserEntity getFirstUserByEmail(String realmId, String email) {
        List<UserEntity> users = em.createQuery("select u from UserEntity u \n" +
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
        List<UserEntity> users = em.createQuery("select u from UserEntity u \n" +
                "  where u.realmId = :realmId and u.username = :username ", UserEntity.class)
                .setParameter("realmId", realmId)
                .setParameter("username", username)
                .getResultList();
        if (users != null && users.size() > 0) {
            return users.get(0);
        }
        return null;
    }

    @Deprecated
    public List<Tuple> getTupleUsersByParametersWithoutGrouping(
            String realm,
            String search,
            String searchUser,
            String searchToms,
            String sortField,
            boolean sortAsc,
            int pageNum,
            int pageSize,
            List<String> includeOnlyIDs
    ) {

        if (search != null && !search.isEmpty())
            search = "%" + search + "%";

       String queryStr =        "select " +
                        "       UE.ID         as user_id,\n" +
                        "       UE.USERNAME   as username,\n" +
                        "       UE.FIRST_NAME as first_name,\n" +
                        "       UE.LAST_NAME  as last_name,\n" +
                        "       UE.EMAIL      as email,\n" +
                        "       UA.VALUE      as phone,\n" +
                        "       UE.ENABLED    as enabled,\n" +
                        "       UP.id         as user_post_id,\n" +
                        "       UP.TOMS_ID    as toms_id,\n" +
                        "       C.NAME        as org,\n" +
                        "       UP.DMP_ID     as dmp_id,\n" +
                        "       UP.ROLE_ID    as role_id,\n" +
                        "       UPR.NAME      as role_name,\n" +
                        "       ESR.ID        as system_role_id,\n" +
                        "       ESR.NAME      as system_role,\n" +
                        "       ES.ID         as system_id,\n" +
                        "       ES.NAME       as system_name,\n" +
                        "       ES.LABEL      as system_label\n " +
                        "from USER_ENTITY UE\n" +
                        "         left join USER_ATTRIBUTE UA on UE.ID = UA.USER_ID and UA.NAME = 'phone'\n" +
                        "         left join USER_POST UP on UE.ID = UP.USER_ID\n" +
                        "         left join USER_POST_ROLE UPR on UP.ROLE_ID = UPR.ID\n" +
                        "         left join USERPOST_EXT_SYSTEM_ROLE UESR on UP.ID = UESR.USER_POST_ID\n" +
                        "         left join EXT_SYSTEM_ROLE ESR on UESR.EXT_SYSTEM_ROLE_ID = ESR.ID\n" +
                        "         left join EXTERNAL_SYSTEM ES on ESR.SYSTEM_ID = ES.ID\n" +
                        "         left join CUSTOMER C on C.ID = UP.TOMS_ID \n" +
                        "WHERE UE.REALM_ID = :realm\n" +
                        "  AND (:search is null or :search = '' or\n" +
                        "    UE.EMAIL LIKE :search OR\n" +
                        "    UE.FIRST_NAME LIKE :search OR\n" +
                        "    UA.VALUE LIKE :search OR\n" +
                        "    UE.USERNAME LIKE :search\n" +
                        "  )\n" +
                        "  AND (:searchUser is null or :searchUser = '' or UE.ID = :searchUser )\n" +
                        "  AND (:searchToms is null or :searchToms = '' or UP.TOMS_ID = :searchToms)\n" +
                        getIdList(includeOnlyIDs) +
                        getSort(sortField, sortAsc) +
                        getLimit(pageNum, pageSize);

        Query query = em.createNativeQuery(
                queryStr
                , Tuple.class)
                .setParameter("search", search)
                .setParameter("searchUser", searchUser)
                .setParameter("searchToms", searchToms)
                .setParameter("realm", realm);

        return query.getResultList();
    }

    public long getTotalUsersByParameters(String realm, String search, String searchUser, String searchToms) {
        Query query = em.createQuery(
                "select count(UE)  " +
                        "from UserEntity UE\n" +
                        "         left join UserAttributeEntity UA on UE = UA.user AND UA.name = 'phone'\n" +
                        "         left join UserPostEntity UP on UE = UP.user \n" +
                        "WHERE UE.realmId = :realm\n" +
                        "and (:search is null or :search = '' or (UE.email LIKE CONCAT('%', :search, '%') OR\n" +
                        "                                         UE.firstName LIKE CONCAT('%', :search, '%') OR\n" +
                        "                                         UE.lastName LIKE CONCAT('%', :search, '%') OR\n" +
                        "                                         UE.username LIKE CONCAT('%', :search, '%') OR\n" +
                        "                                         UA.value LIKE CONCAT('%', :search, '%')))\n" +
                        "and (:searchUser is null or :searchUser = '' or UE.id = :searchUser)\n" +
                        "and (:searchToms is null or :searchToms = '' or UP.customer.id = :searchToms)\n")
                .setParameter("search", search)
                .setParameter("searchUser", searchUser)
                .setParameter("searchToms", searchToms)
                .setParameter("realm", realm);

        return Long.parseLong(query.getSingleResult().toString());
    }

    public List<UserSummaryView> findUsersByParameters(
            String realm,
            String search,
            String searchUser,
            String searchToms,
            String sortField,
            boolean sortAsc,
            int pageNum,
            int pageSize
    ) {
        if (search != null && !search.isEmpty())
            search = "%" + search + "%";

        Query query = em.createQuery(
                "select distinct new ru.alamics.sso.jpa.model.UserSummaryView(UE.id, " +
                        "                                                         UE.username," +
                        "                                                         UE.firstName, " +
                        "                                                         UE.lastName, " +
                        "                                                         UE.email, " +
                        "                                                         UA.value, " +
                        "                                                         UE.enabled) " +
                        "from UserEntity UE\n" +
                        "         left join UserAttributeEntity UA on UE = UA.user AND UA.name = 'phone'\n" +
                        "         left join UserPostEntity UP on UE = UP.user \n" +
                        "WHERE UE.realmId = :realm\n" +
                        "and (:search is null or :search = '' or (UE.email LIKE :search OR\n" +
                        "                                         UE.firstName LIKE :search OR\n" +
                        "                                         UA.value LIKE :search OR\n" +
                        "                                         UE.username LIKE :search ))\n" +
                        "and (:searchUser is null or :searchUser = '' or UE.id = :searchUser)\n" +
                        "and (:searchToms is null or :searchToms = '' or UP.customer.id = :searchToms)\n" +
                        getSort(sortField, sortAsc)
                , UserSummaryView.class)
                .setParameter("search", search)
                .setParameter("searchUser", searchUser)
                .setParameter("searchToms", searchToms)
                .setParameter("realm", realm);

        pageNum = Math.max(1, pageNum);
        pageSize = Math.max(1, pageSize);

        query.setFirstResult((pageNum - 1) * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    public List<UserSummaryView> findUsersByName(
            String realm,
            String search,
            String searchUser,
            String searchToms,
            String sortField,
            boolean sortAsc,
            int pageNum,
            int pageSize
    ) {
        Query query = em.createNativeQuery(
                "select UE.id, " +
                        "                                                         UE.username," +
                        "                                                         UE.firstName, " +
                        "                                                         UE.lastName, " +
                        "                                                         UE.email, " +
                        "                                                         null, " +
                        "                                                         UE.enabled " +
                        "from UserEntity UE \n" +
                        "WHERE UE.realm = :realm \n" +
                        "and (:search is null or :search = '' or MATCH(UE.email, UE.firstName, UE.username) AGAINST(:search IN BOOLEAN MODE)) \n" +
                        "and (:searchUser is null or :searchUser = '' or UE.id = :searchUser) \n" +
                        "and (:searchToms is null or :searchToms = '' or exists( \n" +
                        "  select UP.id \n" +
                        "  from UserPostEntity UP \n" +
                        "  where UP.user = UE and UP.customer.id = :searchToms \n" +
                        ")) \n" +
                        getSort(sortField, sortAsc)
                , UserSummaryView.class)
                .setParameter("search", search)
                .setParameter("searchUser", searchUser)
                .setParameter("searchToms", searchToms)
                .setParameter("realm", realm);

        pageNum = Math.max(1, pageNum);
        pageSize = Math.max(1, pageSize);

        query.setFirstResult((pageNum - 1) * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    public List<UserSummaryView> findUsersByPhone(
            String realm,
            String searchPhone,
            String sortField,
            boolean sortAsc,
            int pageNum,
            int pageSize
    ) {
        Query query = em.createNativeQuery(
                "select UE.id, " +
                        "                                                    UE.username," +
                        "                                                    UE.firstName, " +
                        "                                                    UE.lastName, " +
                        "                                                    UE.email, " +
                        "                                                    UA.value, " +
                        "                                                    UE.enabled " +
                        "from UserEntity UE \n" +
                        "join UserAttributeEntity UA on UE = UA.user \n" +
                        "where UA.name = 'phone' and MATCH(UA.value) AGAINST(:searchPhone IN BOOLEAN MODE) \n" +
                        getSort(sortField, sortAsc)
                , UserSummaryView.class)
                .setParameter("searchPhone", searchPhone)
                .setParameter("realm", realm);

        pageNum = Math.max(1, pageNum);
        pageSize = Math.max(1, pageSize);

        query.setFirstResult((pageNum - 1) * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    private String getSort(String sortField, boolean sortAsc) {
        String sort = "";
        if (SORT_FIELD_NAME.equalsIgnoreCase(sortField)) {
            sort += " ORDER BY first_name ";
        } else if (SORT_FIELD_EMAIL.equalsIgnoreCase(sortField)) {
            sort += " ORDER BY email ";
        }
        if (!sort.isEmpty() && !sortAsc) {
            sort += " DESC";
        }
        return sort;
    }

    private String getIdList(List<String> includeOnlyIDs) {

        if (includeOnlyIDs == null || includeOnlyIDs.isEmpty())
            return "";

        String res = " AND (UE.ID in (\n";
        StringBuilder listStr = new StringBuilder();
        for (String id : includeOnlyIDs) {

            if (listStr.length() > 0)
                listStr.append(",\n");

            listStr.append("'").append(id).append("'");
        }
        res += listStr.toString() + ")) \n";

        return res;
    }

    private String getLimit(int page, int limit) {

        page = Math.max(1, page);
        limit = Math.max(1, limit);
        // TODO max limit ?

        return " LIMIT " + limit + " OFFSET " + (page - 1) * limit;
    }
}