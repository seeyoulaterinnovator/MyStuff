package ru.alamics.sso.keycloak.manager;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import lombok.Builder;
import lombok.NonNull;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import ru.alamics.sso.jpa.model.CustomUserAdapter;
import ru.alamics.sso.keycloak.consts.RealmNames;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

@Provider
@PreMatching
public class ManagerUserAdminRequestInterceptor implements ContainerRequestFilter {
    public static final String DISABLE_STRICT_AUTH_PROPERTY = "X-Manager-DisableStrictAuth";

    private static final String USER_ID_REGEX = "[a-f0-9\\-]+";

    private static final List<Rule> RULES = List.of(
            Rule.builder()
                    .pathPattern(Pattern.compile(
                            "/admin/realms/" + RealmNames.MANAGER + "/users/" + USER_ID_REGEX + "(" +
                                    "|/role-mappings/realm/composite" +
                                    "|/groups" +
                                    ")"
                    ))
                    .disableStrictAuth(true)
                    .userId(uri -> uri.getPathSegments().get(4).getPath())
                    .build()
    );

    @Context
    KeycloakSession session;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        for(Rule rule : RULES) {
            if(rule.pathPattern.matcher(requestContext.getUriInfo().getPath()).matches()) {
                filter(requestContext, rule);
                return;
            }
        }
    }

    void filter(ContainerRequestContext requestContext, Rule rule) {
        UserModel user = session.getProvider(UserProvider.class)
                .getUserById(session.getContext().getRealm(), rule.userId.apply(requestContext.getUriInfo()));

        if(!(user instanceof CustomUserAdapter)) return;

        String userRealm = ((CustomUserAdapter) user).getRealm().getName();

        if (userRealm.equals(Config.getAdminRealm()) || userRealm.equals(RealmNames.MANAGER)) return;

        if(rule.disableStrictAuth) requestContext.setProperty(DISABLE_STRICT_AUTH_PROPERTY, true);

        requestContext.setRequestUri(
                requestContext.getUriInfo()
                        .getRequestUriBuilder()
                        .replacePath(requestContext.getUriInfo().getBaseUri().getPath())
                        .path(requestContext.getUriInfo().getPath()
                                .replaceFirst("/admin/", "/admin-manager/"))
                        .build()
        );
    }

    @Builder
    static class Rule {
        final Pattern pathPattern;

        final boolean disableStrictAuth;

        @NonNull
        final Function<UriInfo, String> userId;
    }
}
