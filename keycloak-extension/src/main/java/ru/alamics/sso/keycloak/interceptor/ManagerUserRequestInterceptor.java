package ru.alamics.sso.keycloak.interceptor;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.keycloak.services.managers.RealmManager;
import ru.alamics.sso.jpa.model.CustomUserAdapter;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Provider
@PreMatching
public class ManagerUserRequestInterceptor implements ContainerRequestFilter {
    private static final String MANAGER_REALM = "manager";

    private static final List<Pattern> PATTERNS = Stream.of(
                    "/admin/realms/([a-zA-Z0-9]+)/users/([a-f0-9\\-]+)(|/role-mappings/realm/composite)",
                    "/admin/realms/([a-zA-Z0-9]+)/ui-ext/effective-roles/users/([a-f0-9\\-]+)"
            )
            .map(Pattern::compile)
            .toList();

    @Context
    KeycloakSession session;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        Parse parse = parse(requestContext);

        if(parse == null) return;

        if(!parse.getRealm().equals(MANAGER_REALM)) return;

        UserModel user = session.getProvider(UserProvider.class)
                .getUserById(session.getContext().getRealm(), parse.getUserId());

        if(!(user instanceof CustomUserAdapter)) return;

        RealmModel userRealm = ((CustomUserAdapter) user).getRealm();

        if (RealmManager.isAdministrationRealm(userRealm)) return;

        if (userRealm.getName().equals(MANAGER_REALM)) return;

        // TODO custom role checks

        session.getContext().setRealm(userRealm);
    }

    Parse parse(ContainerRequestContext requestContext) {
        for(Pattern pattern : PATTERNS) {
            Matcher matcher = pattern.matcher(requestContext.getUriInfo().getPath());
            if(matcher.matches()) {
                return Parse.builder()
                        .realm(matcher.groupCount() >= 1 ? matcher.group(1) : "")
                        .userId(matcher.groupCount() >= 2 ? matcher.group(2) : "")
                        .build();
            }
        }
        return null;
    }

    @Data
    @AllArgsConstructor
    @Builder
    static class Parse {
        String realm;

        String userId;
    }
}
