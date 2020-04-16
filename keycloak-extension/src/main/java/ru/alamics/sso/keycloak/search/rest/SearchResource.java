package ru.alamics.sso.keycloak.search.rest;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.services.validation.Validation;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.registration.mapper.DataMapper;
import ru.alamics.sso.registration.service.UserFindService;
import ru.alamics.sso.user.web.UserSearch;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class SearchResource {

    protected KeycloakSession session;
    private UserFindService userFindService;

    public SearchResource(KeycloakSession session) {
        this.session = session;
        try {
            this.userFindService = (UserFindService) new InitialContext().lookup("java:global/domru-sso/" + UserFindService.class.getSimpleName());
        } catch (NamingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Something wrong with context");
        }
    }

    // TODO может быть работать через кэш ?
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response getUsersInfoWithoutGrouping(@QueryParam("search") String search, @QueryParam("searchUser") String searchUser,
                                                @QueryParam("searchToms") String searchToms, @QueryParam("sortField") String sortField,
                                                @QueryParam("sortAsc") boolean sortAsc, @QueryParam("searchRealm") String searchRealm) {
        session.userCache().clear();
        if (searchRealm == null || searchRealm.isEmpty()) {
            searchRealm = "user";
        }
        return JsonResponse.success()
                .addResult("users-info",
                        userFindService.getUsersByParametersWithoutGrouping(searchRealm, search, searchUser, searchToms, sortField, sortAsc))
                .build();
    }

    // TODO 2 раза ходит в бд за списком и за кол-вом
    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response getUsersInfo(@QueryParam("search") String search, @QueryParam("searchUser") String searchUser,
                                 @QueryParam("searchToms") String searchToms, @QueryParam("sortField") String sortField,
                                 @QueryParam("sortAsc") boolean sortAsc, @QueryParam("searchRealm") String searchRealm,
                                 @QueryParam("pageNum") int pageNum, @QueryParam("pageSize") int pageSize) {
        if (searchRealm == null || searchRealm.isEmpty()) {
            searchRealm = "user";
        }

        log.info("getUsersInfo 1");

        List<UserSearch> users = userFindService.getUsersByParameters(searchRealm, search, searchUser, searchToms, sortField, sortAsc, pageNum, pageSize);

        log.info("getUsersInfo 2");

        long total = 200; // userFindService.getTotalUsersByParameters(searchRealm, search, searchUser, searchToms);

        log.info("getUsersInfo 3");

        Response respB = JsonResponse.success()
                .addResult("users-info", users)
                .addResult("page-info",
                        DataMapper.toPageDto(users,
                                total,
                                pageNum, pageSize))
                .build();

        log.info("getUsersInfo 4");

        return respB;
    }

    @GET
    @Path("/attribute")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response findUserByAttribute(@QueryParam("phone") String phone, @QueryParam("excludedUserId") String excludedUserId) {
        if (Validation.isBlank(phone)) {
            return JsonResponse.success().addResult("foundUserId", null).build();
        }

        if (Validation.isBlank(excludedUserId)) {
            throw new WebApplicationException(
                    Response.status(HttpURLConnection.HTTP_BAD_REQUEST)
                            .entity("excludeUserId parameter is mandatory")
                            .build()
            );
        }
        //fixme сквозной поиск по всем реалмам
        UserEntity user = userFindService.getUserByPhoneAndExcludedUserId(phone, excludedUserId);
        return JsonResponse.success().addResult("foundUserId", user == null ? null : user.getId()).build();
    }

    @GET
    @Path("/accessible-realms")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public List<String> getAccessibleRealms() {
        return session.realms().getRealms().stream()
                .filter(o -> {
                    switch (session.getContext().getRealm().getName()) {
                        case "master":
                            return true;
                        case "user":
                        case "manager":
                            if (o.getName().equalsIgnoreCase("user")) {
                                return true;
                            }
                            return false;
                    }
                    return false;
                })
                .map(RealmModel::getName)
                .collect(Collectors.toList());
    }
}
