package ru.alamics.sso.keycloak.manager;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import lombok.Builder;
import lombok.Singular;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmProvider;
import ru.alamics.sso.keycloak.GeneralRealm;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

@Provider
@PreMatching
public class ManagerRealmsAdminRequestInterceptor implements ContainerRequestFilter {
    private static final String SEARCH_REALM_PARAM = "searchRealm";

    private static final List<Rule> RULES = List.of(
            Rule.builder()
                    .pathPattern(Pattern.compile("/admin/realms/" + GeneralRealm.MANAGER + "/sessions(|/.*)"))
                    .pathPattern(Pattern.compile("/admin/realms/" + GeneralRealm.MANAGER + "/identity-provider/instances/.*"))
                    .method(HttpMethod.GET)
                    .disableStrictAuth(true)
                    .replaceContextRealm(true)
                    .build(),
            Rule.builder()
                    .pathPattern(Pattern.compile("/admin/realms/" + GeneralRealm.MANAGER + "/sessions/[a-f0-9\\-]+"))
                    .method(HttpMethod.DELETE)
                    .disableStrictAuth(true)
                    .replaceContextRealm(true)
                    .build()
    );

    @Context
    KeycloakSession session;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        for(Rule rule : RULES) {
            if(rule.pathPatterns.stream().anyMatch(p -> p.matcher(requestContext.getUriInfo().getPath()).matches())
                    && (rule.methods.isEmpty() || rule.methods.contains(requestContext.getMethod()))) {
                filter(requestContext, rule);
                return;
            }
        }
    }

    void filter(ContainerRequestContext requestContext, Rule rule) {
        String searchRealm = requestContext.getUriInfo().getQueryParameters().getFirst(SEARCH_REALM_PARAM);

        if(searchRealm == null || searchRealm.isBlank()) return;

        if (searchRealm.equals(Config.getAdminRealm()) || searchRealm.equals(GeneralRealm.MANAGER)) return;

        if(session.getProvider(RealmProvider.class).getRealmByName(searchRealm) == null) return;

        if(rule.disableStrictAuth) {
            requestContext.setProperty(ManagerRequestProperties.DISABLE_STRICT_ADMIN_AUTH, true);
        }

        if(rule.replaceContextRealm) {
            requestContext.setProperty(ManagerRequestProperties.ADMIN_CONTEXT_REALM, searchRealm);
        }

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

        @Singular
        final List<String> methods;

        final boolean disableStrictAuth;

        final boolean replaceContextRealm;
    }
}
