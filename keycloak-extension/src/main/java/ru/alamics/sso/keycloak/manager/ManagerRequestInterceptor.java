package ru.alamics.sso.keycloak.manager;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import ru.alamics.sso.jpa.model.CustomUserAdapter;
import ru.alamics.sso.keycloak.consts.RealmNames;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Provider
@PreMatching
public class ManagerRequestInterceptor implements ContainerRequestFilter {
    public static final String USER_REALM_PROPERTY = ManagerRequestInterceptor.class.getSimpleName() + "-UserRealm";

    private static final String REALM_PATTERN = "([a-zA-Z0-9]+)";

    private static final String USER_ID_PATTERN = "([a-f0-9\\-]+)";

    private static final List<Pattern> PATTERNS = List.of(
            Pattern.compile("/admin/realms/" + REALM_PATTERN + "/users/" + USER_ID_PATTERN
                    + "(|/role-mappings/realm/composite|groups)"),
            Pattern.compile("/admin/realms/" + REALM_PATTERN + "/ui-ext/effective-roles/users/" + USER_ID_PATTERN)
    );

    @Context
    KeycloakSession session;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        for(Pattern pattern : PATTERNS) {
            Matcher matcher = pattern.matcher(requestContext.getUriInfo().getPath());
            if(matcher.matches()) {
                String realm = matcher.group(1);
                String userId = matcher.group(2);
                filter(requestContext, realm, userId);
                return;
            }
        }
    }

    void filter(ContainerRequestContext requestContext, String realm, String userId) {
        if (!realm.equals(RealmNames.MANAGER)) return;

        UserModel user = session.getProvider(UserProvider.class).getUserById(session.getContext().getRealm(), userId);

        if(!(user instanceof CustomUserAdapter)) return;

        RealmModel userRealm = ((CustomUserAdapter) user).getRealm();

        if (userRealm.getName().equals(Config.getAdminRealm())
                || userRealm.getName().equals(RealmNames.MANAGER)) return;

        // TODO custom role checks

        requestContext.setProperty(USER_REALM_PROPERTY, userRealm.getName());

        requestContext.setRequestUri(
                requestContext.getUriInfo()
                        .getRequestUriBuilder()
                        .replacePath(requestContext.getUriInfo().getBaseUri().getPath())
                        .path(
                                requestContext.getUriInfo()
                                        .getPath()
                                        .replaceFirst(
                                                "/admin/realms/" + realm + "/",
                                                "/admin-manager/realms/" + userRealm.getName() + "/"
                                        ))
                        .build()
        );
    }

    @Data
    @AllArgsConstructor
    @Builder
    static class Parse {
        String realm;

        String userId;
    }
}
