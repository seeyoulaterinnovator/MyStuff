package ru.alamics.sso.keycloak.search.rest;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.validation.Validation;
import ru.alamics.sso.keycloak.GeneralRealm;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.registration.mapper.DataMapper;
import ru.alamics.sso.registration.service.UserFindService;
import ru.alamics.sso.user.web.UserSearch;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class SearchResource {

    public static final String VIEW_ROLE_PATTERN = "view-%s-realm";
    private final UserFindService userFindService;
    private final AdminAuth adminAuth;
    protected KeycloakSession session;

    public SearchResource(KeycloakSession session, AdminAuth adminAuth) {
        this.session = session;
        this.adminAuth = adminAuth;
        this.userFindService = Lookup.lookup(UserFindService.class);
    }

    private static String formatViewRole(RealmModel realm) {
        return String.format(VIEW_ROLE_PATTERN, realm.getName());
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response getUsersInfoWithoutGrouping(@QueryParam("search") String search, @QueryParam("searchUser") String searchUser,
                                                @QueryParam("searchToms") String searchToms, @QueryParam("sortField") String sortField,
                                                @QueryParam("sortAsc") boolean sortAsc, @QueryParam("searchRealm") String searchRealm,
                                                @DefaultValue("1") @QueryParam("pageNum") int pageNum, @DefaultValue("100") @QueryParam("pageSize") int pageSize) {

        session.userCache().clear();
        searchRealm = getRealm(searchRealm);

        return JsonResponse.success()
                .addResult("users-info",
                        userFindService.getUsersByParametersWithoutGrouping(searchRealm, search, searchUser, searchToms, sortField, sortAsc, pageNum, pageSize, null))
                .build();
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response getUsersInfo(@QueryParam("search") String search, @QueryParam("searchUser") String searchUser,
                                 @QueryParam("searchToms") String searchToms, @QueryParam("searchPhone") String searchPhone,
                                 @QueryParam("sortField") String sortField, @QueryParam("sortAsc") boolean sortAsc,
                                 @QueryParam("searchRealm") String searchRealm,
                                 @QueryParam("pageNum") int pageNum, @QueryParam("pageSize") int pageSize) {

        searchRealm = getRealm(searchRealm);

        session.userCache().clear();

        log.info("getUsersInfo 1");

        List<UserSearch> users = userFindService.getUsersByParameters(searchRealm, search, searchUser, searchToms, searchPhone, sortField, sortAsc, pageNum, pageSize);

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

    private String getRealm(String searchRealm) {
        String rawPath = session.getContext().getUri().getAbsolutePath().getRawPath();
        if (searchRealm == null || searchRealm.isEmpty()) {
            List<String> path = Arrays.asList(rawPath.split("/"));
            int realms = path.indexOf("realms");
            String realm = path.get(realms + 1);
            searchRealm = realm == null ? "user" : realm;
        }
        return searchRealm;
    }

    @GET
    @Path("/attribute")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response findUserByAttribute(@QueryParam("phone") String phone, @QueryParam("excludedUserId") String excludedUserId, @QueryParam("realmId") String realmId) {
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
        UserEntity user = userFindService.getUserByPhoneAndExcludedUserId(realmId, phone, excludedUserId);
        return JsonResponse.success().addResult("foundUserId", user == null ? null : user.getId()).build();
    }

    @GET
    @Path("/accessible-realms")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public List<String> getAccessibleRealms() {
        final List<RealmModel> realms = session.realms().getRealms();
        final Set<RoleModel> userRoles = adminAuth.getUser().getRoleMappings();
        List<String> userViewRoles = new ArrayList<>();
        for (RealmModel realm : realms) {
            for (RoleModel role : userRoles) {
                if (String.format(VIEW_ROLE_PATTERN, realm.getName()).equalsIgnoreCase(role.getName())) {
                    userViewRoles.add(role.getName());
                }
            }
        }
        return realms.stream()
                .filter(getPredicateByViewRoles(userRoles, userViewRoles.isEmpty()))
                .map(RealmModel::getName)
                .collect(Collectors.toList());
    }

    private Predicate<RealmModel> getPredicateByViewRoles(Set<RoleModel> roles, boolean userDontHaveViewRoles) {
        final String currentRealm = session.getContext().getRealm().getName();
        if (userDontHaveViewRoles) {
            return realm -> {
                switch (currentRealm) {
                    case GeneralRealm.MASTER:
                        return true;
                    case GeneralRealm.MANAGER:
                        return GeneralRealm.REALMS.stream().noneMatch(realm.getName()::equalsIgnoreCase);
                }
                return false;
            };
        } else {
            if (GeneralRealm.MASTER.equalsIgnoreCase(currentRealm)) {
                return realm -> true;
            } else {
                return realm -> roles.stream()
                        .anyMatch(role -> formatViewRole(realm).equalsIgnoreCase(role.getName()));
            }
        }
    }
}
