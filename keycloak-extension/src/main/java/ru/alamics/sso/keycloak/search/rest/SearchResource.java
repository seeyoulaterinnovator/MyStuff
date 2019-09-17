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
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedList;
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

    public List<UserDto> getUsers(String search, String searchUser, String searchToms) {
        List<Tuple> tuples = getEM().createNativeQuery(
                "select UE.ID         as user_id,\n" +
                        "       UE.USERNAME   as username,\n" +
                        "       UE.FIRST_NAME as first_name,\n" +
                        "       UE.LAST_NAME  as last_name,\n" +
                        "       UE.EMAIL      as email,\n" +
                        "       UA.VALUE      as phone,\n" +
                        "       UP.id         as user_post_id,\n" +
                        "       UP.TOMS_ID    as toms_id,\n" +
                        "       UP.DMP_ID     as dmp_id,\n" +
                        "       UP.ROLE_ID    as role_id,\n" +
                        "       UPR.NAME      as role_name,\n" +
                        "       ESR.ID        as system_role_id,\n" +
                        "       ESR.NAME      as system_role,\n" +
                        "       ES.ID         as system_id,\n" +
                        "       ES.NAME       as system_name\n" +
                        "from USER_ENTITY UE\n" +
                        "         join USER_ATTRIBUTE UA on UE.ID = UA.USER_ID\n" +
                        "         left outer join USER_POST UP on UE.ID = UP.USER_ID\n" +
                        "         left outer join USER_POST_ROLE UPR on UP.ROLE_ID = UPR.ID\n" +
                        "         left outer join USERPOST_EXT_SYSTEM_ROLE UESR on UP.ID = UESR.USER_POST_ID\n" +
                        "         left outer join EXT_SYSTEM_ROLE ESR on UESR.EXT_SYSTEM_ROLE_ID = ESR.ID\n" +
                        "         left outer join EXTERNAL_SYSTEM ES on ESR.SYSTEM_ID = ES.ID\n" +
                        "WHERE UE.REALM_ID = 'user'\n" +
                        "  AND UA.NAME = 'phone'\n" +
                        "  AND CASE\n" +
                        "          WHEN :search is not null and :search != '' then (\n" +
                        "                  UE.ID LIKE CONCAT('%', :search, '%') OR\n" +
                        "                  UE.EMAIL LIKE CONCAT('%', :search, '%') OR\n" +
                        "                  UE.FIRST_NAME LIKE CONCAT('%', :search, '%') OR\n" +
                        "                  UE.LAST_NAME LIKE CONCAT('%', :search, '%') OR\n" +
                        "                  UE.USERNAME LIKE CONCAT('%', :search, '%') OR\n" +
                        "                  UA.VALUE LIKE CONCAT('%', :search, '%')\n" +
                        "              )\n" +
                        "          else UE.ID LIKE '%' end\n" +
                        "  AND CASE\n" +
                        "          WHEN :searchUser is not null and :searchUser != '' then (UP.USER_ID = :searchUser)\n" +
                        "          else UP.USER_ID LIKE '%' OR UP.USER_ID is null end\n" +
                        "  AND CASE\n" +
                        "          WHEN :searchToms is not null and :searchToms != '' then (UP.TOMS_ID = :searchToms)\n" +
                        "          else UP.TOMS_ID LIKE '%' OR UP.TOMS_ID is null end", Tuple.class)
                .setParameter("search", search)
                .setParameter("searchUser", searchUser)
                .setParameter("searchToms", searchToms)
                .getResultList();
        return DataMapper.toUserDtoList(tuples);
    }
}
