package ru.alamics.sso.keycloak.auth.post;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import ru.alamics.sso.auth.UserRole;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.keycloak.search.rest.SearchResource;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Map;

import static ru.alamics.sso.registration.model.UserConstants.*;

@Slf4j
public class AttributesForm implements Authenticator {
    private static final String FORM = "attributes.ftl";

    private final UserRole role;

    public AttributesForm (UserRole role) {
        this.role = role;
    }

    @Override
    public void authenticate (AuthenticationFlowContext context) {
        final String DEBUG_STR = "authenticate";
        var authSession = context.getAuthenticationSession();
        log.info("{}: frame={}", DEBUG_STR, authSession.getAuthNote(I_FRAME));
        var uriInfo = context.getUriInfo();
        log.info("{}: getParams={}", DEBUG_STR, uriInfo.getQueryParameters());
        String frame = uriInfo.getQueryParameters().getFirst(I_FRAME);
        boolean isAuth = "1".equals(authSession.getAuthNote(AUTH_FORM_SUCCESS));//it`s magick

        if( frame != null || isAuth ) {
            var session = context.getSession();
            var searchResource = new SearchResource(session);
            var user = context.getUser();
            var response = searchResource.getUsersInfo( "", user.getId(), "");
            JsonResponse body = (JsonResponse) response.getEntity();
            var attributes = body.getResults();
            if(attributes.get("users-info") == null) {
                context.success();
            } else {
                Response challenge = createForm(context, attributes);
                context.challenge(challenge);
            }
        } else {
            context.success();
        }


    }

    private Response createForm(AuthenticationFlowContext context,  Map<String, Object> attributes) {
        LoginFormsProvider form = context.form();
        if(attributes.size() > 0) {
            form.setAttribute("posts", attributes.get("users-info"));
        }

        return form.createForm(FORM);
    }

    @Override
    public void action (AuthenticationFlowContext context) {
        var authSession = context.getAuthenticationSession();
        role.roleSetting(context);
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        final String tomsId = formData.get("tomsId").get(0);
        var user = context.getUser();
        user.setAttribute(ATTR_TOMS_NAME, Collections.singletonList(tomsId));
        authSession.setAuthNote(AUTH_FORM_SUCCESS, "0");
        context.success();
    }

    @Override
    public boolean requiresUser () {
        return false;
    }

    @Override
    public boolean configuredFor (KeycloakSession session, RealmModel realm, UserModel user) {
        return false;
    }

    @Override
    public void setRequiredActions (KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public void close () {

    }
}
