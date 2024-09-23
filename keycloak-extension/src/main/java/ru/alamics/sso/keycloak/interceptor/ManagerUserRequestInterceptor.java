package ru.alamics.sso.keycloak.interceptor;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.keycloak.services.managers.RealmManager;
import ru.alamics.sso.jpa.model.CustomUserAdapter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Provider
@PreMatching
public class ManagerUserRequestInterceptor implements ContainerRequestFilter {
    @Context
    KeycloakSession session;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        Matcher matcher = Pattern.compile("/admin/realms/([a-zA-Z0-9]+)/users/([a-f0-9\\-]+)(.*)")
                .matcher(requestContext.getUriInfo().getPath());

        if(!matcher.matches()) return;

        String realm = matcher.group(1);
        String userId = matcher.group(2);
        String path = matcher.group(3);

        UserModel user = session.getProvider(UserProvider.class).getUserById(session.getContext().getRealm(), userId);

        if(!(user instanceof CustomUserAdapter)) return;

        RealmModel userRealm = ((CustomUserAdapter) user).getRealm();

        if (RealmManager.isAdministrationRealm(userRealm)) return;

        if(!realm.equals("manager")) return;

        if(!path.equals("/")) return;

        // TODO custom role checks

        session.getContext().setRealm(userRealm);
    }
}
