package ru.alamics.sso.keycloak.search.rest;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.mapper.DataMapper;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.keycloak.search.dto.UserDto;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Slf4j
public class SearchResource {


    protected KeycloakSession session;

    public SearchResource(KeycloakSession session) {
        this.session = session;
    }

    private EntityManager getEM() {
        return session.getProvider(JpaConnectionProvider.class).getEntityManager();
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response getUsersInfo(@QueryParam("search") String search, @QueryParam("searchUser") String searchUser,
                            @QueryParam("searchToms") String searchToms) {
        return JsonResponse.success()
                .addResult("users-info", getUsers(search, searchUser, searchToms))
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<UserDto> getUsers(String search, String searchUser, String searchToms) {
        List<Tuple> tuples = getEM().createNativeQuery(
                "select UE.ID as user_id,\n" +
                "       UE.USERNAME as username,\n" +
                "       UE.FIRST_NAME as first_name,\n" +
                "       UE.LAST_NAME as last_name,\n" +
                "       UE.EMAIL as email,\n" +
                "       UA.VALUE as phone,\n" +
                "       up.id as access_id,\n" +
                "       up.TOMS_ID as toms_id,\n" +
                "       up.DMP_ID as dmp_id,\n" +
                "       up.ROLE_ID as role_id,\n" +
                "       upr.NAME as role_name\n" +
                "from USER_POST up\n" +
                "         join USER_POST_ROLE upr on up.ROLE_ID = upr.ID\n" +
                "         join USER_ENTITY UE on up.USER_ID = UE.ID\n" +
                "         join USER_ATTRIBUTE UA on UE.ID = UA.USER_ID and UA.NAME = 'phone'\n" +
                "where up.USER_ID = :searchUser\n" +
                "   or up.TOMS_ID = :searchToms", Tuple.class)
                .setParameter("searchUser", searchUser)
                .setParameter("searchToms", searchToms)
                .getResultList();
        return DataMapper.toUserDtoList(tuples);
    }
//    private List<UserDto> getUsers(String search, String searchUser, String searchToms) {
//        List<Tuple> tuples = getEM().createNativeQuery(
//                "select users.user_id,\n" +
//                        "       users.USERNAME,\n" +
//                        "       users.FIRST_NAME,\n" +
//                        "       users.LAST_NAME,\n" +
//                        "       users.EMAIL,\n" +
//                        "       users.phone,\n" +
//                        "       user_post.id as access_id,\n" +
//                        "       user_post.toms_id,\n" +
//                        "       user_post.ROLE_ID,\n" +
//                        "       user_post.NAME as role_name\n" +
//                        "from (select ue.id    as user_id,\n" +
//                        "             ue.USERNAME,\n" +
//                        "             ue.FIRST_NAME,\n" +
//                        "             ue.LAST_NAME,\n" +
//                        "             ue.EMAIL,\n" +
//                        "             ua.VALUE as phone\n" +
//                        "      from USER_ENTITY ue\n" +
//                        "               join USER_ATTRIBUTE ua on ue.ID = ua.USER_ID and ue.REALM_ID = 'user' and ua.NAME = 'phone'\n" +
//                        "      where (ue.ID LIKE '%' || :search || '%' OR ue.USERNAME LIKE '%' || :search || '%'\n" +
//                        "          OR ue.FIRST_NAME LIKE '%' || :search || '%' OR ue.LAST_NAME LIKE '%' || :search || '%'\n" +
//                        "          OR ue.EMAIL LIKE '%' || :search || '%' OR ua.VALUE LIKE '%' || :search || '%'\n" +
//                        "                )) as users\n" +
//                        "join\n" +
//                        "     (select up.*,\n" +
//                        "             upr.NAME\n" +
//                        "      from USER_POST up\n" +
//                        "               join USER_POST_ROLE upr on up.ROLE_ID = upr.ID\n" +
//                        "      where up.USER_ID LIKE '%' || :searchUser || '%'\n" +
//                        "        and up.TOMS_ID LIKE '%' || :searchToms || '%')\n" +
//                        "         as user_post\n" +
//                        "ORDER BY users.FIRST_NAME, users.EMAIL", Tuple.class)
//                .setParameter("search", search)
//                .setParameter("searchUser", searchUser)
//                .setParameter("searchToms", searchToms)
//                .getResultList();
//        return DataMapper.toUserDtoList(tuples);
//    }
}
