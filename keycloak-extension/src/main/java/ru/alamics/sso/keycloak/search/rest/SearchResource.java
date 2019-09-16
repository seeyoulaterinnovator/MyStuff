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
                        "       UA.VALUE      as phone\n" +
                        "from USER_ENTITY UE\n" +
                        "         join USER_ATTRIBUTE UA on UE.ID = UA.USER_ID\n" +
                        "WHERE UE.REALM_ID = 'user' AND UA.NAME = 'phone'\n" +
                        "  AND CASE\n" +
                        "          WHEN :search is not null and :search != '' then (\n" +
                        "              UE.ID LIKE CONCAT('%', :search, '%') OR\n" +
                        "              UE.EMAIL LIKE CONCAT('%', :search, '%') OR\n" +
                        "              UE.FIRST_NAME LIKE CONCAT('%', :search, '%') OR\n" +
                        "              UE.LAST_NAME LIKE CONCAT('%', :search, '%') OR\n" +
                        "              UE.USERNAME LIKE CONCAT('%', :search, '%') OR\n" +
                        "              UA.VALUE LIKE CONCAT('%', :search, '%')\n" +
                        "              )\n" +
                        "          else UE.ID LIKE '%' end", Tuple.class)
                .setParameter("search", search)
                .getResultList();
        return DataMapper.toUserDtoList(tuples);
    }
}
