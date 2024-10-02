package ru.alamics.sso.keycloak.manager;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import ru.alamics.sso.jpa.model.CustomUserAdapter;
import ru.alamics.sso.keycloak.GeneralRealm;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

@Provider
@PreMatching
public class ManagerUsersAdminRequestInterceptor implements ContainerRequestFilter {
    private static final String USER_ID_REGEX = "[a-f0-9\\-]+";

    private static final List<Rule> RULES = List.of(
            Rule.builder()
                    .pathPattern(Pattern.compile(
                            "/admin/realms/" + GeneralRealm.MANAGER + "/users/" + USER_ID_REGEX
                                    + "(|/role-mappings/realm/composite)"
                    ))
                    .disableStrictAuth(true)
                    .userId(uri -> uri.getPathSegments().get(4).getPath())
                    .build(),
            Rule.builder()
                    .pathPattern(Pattern.compile(
                            "/admin/realms/" + GeneralRealm.MANAGER + "/users/" + USER_ID_REGEX
                                    + "(/federated-identity|/groups|/consents|/sessions|/logout)(|/.*)"
                    ))
                    .disableStrictAuth(true)
                    .replaceContextRealm(true)
                    .userId(uri -> uri.getPathSegments().get(4).getPath())
                    .build()
    );

    @Context
    KeycloakSession session;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        for(Rule rule : RULES) {
            if(rule.pathPatterns.stream()
                    .anyMatch(pattern -> pattern.matcher(requestContext.getUriInfo().getPath()).matches())) {
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

        if (userRealm.equals(Config.getAdminRealm())) return;

        if(rule.disableStrictAuth) {
            requestContext.setProperty(ManagerRequestProperties.DISABLE_STRICT_ADMIN_AUTH, true);
        }

        if(rule.replaceContextRealm) {
            requestContext.setProperty(ManagerRequestProperties.ADMIN_CONTEXT_REALM, userRealm);
        }

        requestContext.setProperty(ManagerRequestProperties.ADMIN_EVENT_REALM, userRealm);

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
    private static class Rule {
        @Singular
        List<Pattern> pathPatterns;

        final boolean disableStrictAuth;

        final boolean replaceContextRealm;

        @NonNull
        final Function<UriInfo, String> userId;
    }
}
