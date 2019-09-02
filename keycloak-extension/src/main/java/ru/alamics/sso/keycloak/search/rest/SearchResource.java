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

    private List<UserDto> getUsers(String search, String searchUser, String searchToms) {
        List<Tuple> tuples = getEM().createNativeQuery(
                "select users.user_id,\n" +
                        "       users.USERNAME,\n" +
                        "       users.FIRST_NAME,\n" +
                        "       users.LAST_NAME,\n" +
                        "       users.EMAIL,\n" +
                        "       users.phone,\n" +
                        "       access.access_name,\n" +
                        "       access.access_id,\n" +
                        "       access.toms_id,\n" +
                        "       urm.ROLE_ID,\n" +
                        "       kr.NAME as role_name\n" +
                        "from (select ue.id    as user_id,\n" +
                        "             ue.USERNAME,\n" +
                        "             ue.FIRST_NAME,\n" +
                        "             ue.LAST_NAME,\n" +
                        "             ue.EMAIL,\n" +
                        "             ua.VALUE as phone\n" +
                        "      from USER_ENTITY ue\n" +
                        "               join USER_ATTRIBUTE ua on ue.ID = ua.USER_ID and ue.REALM_ID = 'user' and ua.NAME = 'phone'\n" +
                        "      where (ue.ID LIKE '%' || :search || '%' OR ue.USERNAME LIKE '%' || :search || '%'\n" +
                        "          OR ue.FIRST_NAME LIKE '%' || :search || '%' OR ue.LAST_NAME LIKE '%' || :search || '%'\n" +
                        "          OR ue.EMAIL LIKE '%' || :search || '%' OR ua.VALUE LIKE '%' || :search || '%'\n" +
                        "                )) as users\n" +
                        "join (select accessUser.id       as access_id,\n" +
                        "             accessUser.USERNAME as access_name,\n" +
                        "             accessUser.userId,\n" +
                        "             accessToms.toms_id\n" +
                        "      from (select ue.id,\n" +
                        "                   ue.USERNAME,\n" +
                        "                   ua.value as userId\n" +
                        "            from USER_ENTITY ue\n" +
                        "            join USER_ATTRIBUTE ua on ue.ID = ua.USER_ID and ue.REALM_ID = 'access'\n" +
                        "            where (ua.NAME = 'userId' and ua.VALUE LIKE '%' || :searchUser || '%')) as accessUser\n" +
                        "            join\n" +
                        "                (select ue.id,\n" +
                        "                        ue.USERNAME,\n" +
                        "                        ua.VALUE as toms_id\n" +
                        "                from USER_ENTITY ue\n" +
                        "                join USER_ATTRIBUTE ua on ue.ID = ua.USER_ID and ue.REALM_ID = 'access'\n" +
                        "                where (ua.NAME = 'tomsId' and ua.VALUE LIKE '%' || :searchToms || '%')) as accessToms on accessUser.id = accessToms.id\n" +
                        "      ) as access on users.user_id = access.userId\n" +
                        "join USER_ROLE_MAPPING urm on access.access_id = urm.USER_ID\n" +
                        "join KEYCLOAK_ROLE kr on urm.ROLE_ID = kr.ID\n" +
                        "ORDER BY FIRST_NAME, EMAIL", Tuple.class)
                .setParameter("search", search)
                .setParameter("searchUser", searchUser)
                .setParameter("searchToms", searchToms)
                .getResultList();
        return DataMapper.toUserDtoList(tuples);
    }
}
