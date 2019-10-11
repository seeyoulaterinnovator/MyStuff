package ru.alamics.sso.keycloak.search.rest;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.permissions.AdminPermissions;
import ru.alamics.sso.keycloak.mapper.DataMapper;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.keycloak.search.dto.UserDto;
import ru.alamics.sso.registration.service.UserFindService;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class SearchResource {

    private final static String SORT_FIELD_NAME = "firstName";
    private final static String SORT_FIELD_EMAIL = "email";
    protected KeycloakSession session;
    private UserFindService userFindService;
    private AdminPermissionEvaluator auth;

    public SearchResource(KeycloakSession session) {
        this.session = session;
        this.auth = initAuth(session);
        auth.users().requireView();
        try {
            this.userFindService = (UserFindService) new InitialContext().lookup("java:global/domru-sso/" + UserFindService.class.getSimpleName());
        } catch (NamingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Something wrong with context");
        }
    }

    private AdminPermissionEvaluator initAuth(KeycloakSession session) {
        KeycloakContext context = session.getContext();
        AppAuthManager appAuthManager = new AppAuthManager();
        String tokenString = Optional.ofNullable(appAuthManager.extractAuthorizationHeaderToken(context.getRequestHeaders())).orElseThrow(() -> new NotAuthorizedException("Bearer"));

        AccessToken token;
        try {
            JWSInput input = new JWSInput(tokenString);
            token = input.readJsonContent(AccessToken.class);
        } catch (JWSInputException e) {
            throw new NotAuthorizedException("Bearer token format error");
        }

        String issuer = Optional.ofNullable(token.getIssuer()).orElseThrow(() -> new RuntimeException("empty issuer"));
        String realmName = issuer.substring(issuer.lastIndexOf('/') + 1);

        RealmManager realmManager = new RealmManager(session);
        RealmModel realmFromToken = Optional.ofNullable(realmManager.getRealmByName(realmName))
                .orElseThrow(() -> new NotAuthorizedException("Unknown realm in token"));

        session.getContext().setRealm(realmFromToken);//FIXME Подставляем реалм из его токена и валидируем относительно его реалма, иначе authResult кинет NPE, мб возможно сделать аккауратней

        AuthenticationManager.AuthResult authResult = Optional.ofNullable(appAuthManager.authenticateBearerToken(session, realmFromToken))
                .orElseThrow(() -> new NotAuthorizedException("Bearer"));

        ClientModel client = Optional.ofNullable(realmFromToken.getClientByClientId(token.getIssuedFor()))
                .orElseThrow(() -> new NotAuthorizedException("Could not find client for authorization"));

        AdminAuth auth = new AdminAuth(realmFromToken, authResult.getToken(), authResult.getUser(), client);

        AdminPermissionEvaluator realmAuth = AdminPermissions.evaluator(session, realmFromToken, auth);
        return realmAuth;
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
                                 @QueryParam("searchToms") String searchToms, @QueryParam("sortField") String sortField,
                                 @QueryParam("sortAsc") boolean sortAsc, @QueryParam("realm") String realm) {
        if (realm == null || realm.isBlank()) {
            realm = "user";
        }
        return JsonResponse.success()
                .addResult("users-info", getUsers(realm, search, searchUser, searchToms, sortField, sortAsc))
                .build();
    }

    public List<UserDto> getUsers(String realm, String search, String searchUser, String searchToms, String sortField, boolean sortAsc) {
        List<Tuple> tuples = getEM().createNativeQuery(
                "select UE.ID         as user_id,\n" +
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
                .setParameter("realm", realm)
                .getResultList();
        return DataMapper.toUserDtoList(tuples);
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

    @GET
    @Path("/attribute")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response findUserByAttribute(@QueryParam("phone") String phone, @QueryParam("excludeUserId") String excludeUserId) {
        if (phone == null) {
            throw new WebApplicationException(
                    Response.status(HttpURLConnection.HTTP_BAD_REQUEST)
                            .entity("phone parameter is mandatory")
                            .build()
            );
        }

        if (excludeUserId == null) {
            throw new WebApplicationException(
                    Response.status(HttpURLConnection.HTTP_BAD_REQUEST)
                            .entity("excludeUserId parameter is mandatory")
                            .build()
            );
        }
        var user = userFindService.getUserByPhoneAndExcludedUserId(session.getContext().getRealm(), phone, excludeUserId);
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
                            if (o.getName().equalsIgnoreCase("user")) {
                                return true;
                            }
                            return false;
                        case "manager":
                            if (o.getName().equalsIgnoreCase("master")) {
                                return false;
                            }
                            return true;
                    }
                    return false;
                })
                .map(RealmModel::getName)
                .collect(Collectors.toList());
    }

    private AdminAuth authenticateRealmAdminRequest(RealmModel realm) {
        String tokenString = new AppAuthManager().extractAuthorizationHeaderToken(session.getContext().getRequestHeaders());
        if (tokenString == null) throw new NotAuthorizedException("Bearer");
        AccessToken token;
        try {
            JWSInput input = new JWSInput(tokenString);
            token = input.readJsonContent(AccessToken.class);
        } catch (JWSInputException e) {
            throw new NotAuthorizedException("Bearer token format error");
        }

        String realmName = token.getIssuer().substring(token.getIssuer().lastIndexOf('/') + 1);
        RealmManager realmManager = new RealmManager(session);
        RealmModel realmFromToken = realmManager.getRealmByName(realmName);
        if (realmFromToken == null) {
            throw new NotAuthorizedException("Unknown realm in token");
        }

        session.getContext().setRealm(realm);
        AuthenticationManager.AuthResult authResult = new AppAuthManager()
                .authenticateBearerToken(session, realm, session.getContext().getUri(), session.getContext().getConnection(), session.getContext().getRequestHeaders());
        if (authResult == null) {
            log.debug("Token not valid");
            throw new NotAuthorizedException("Bearer");
        }

        ClientModel client = realm.getClientByClientId(token.getIssuedFor());
        if (client == null) {
            throw new NotAuthorizedException("Could not find client for authorization");
        }

        AdminAuth auth = new AdminAuth(realm, authResult.getToken(), authResult.getUser(), client);

        return auth;
    }
}
