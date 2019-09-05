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

@Slf4j
public class AttributesForm implements Authenticator {
    private static final String FORM = "attributes.ftl";

    private final UserRole role;

    public AttributesForm (UserRole role) {
        this.role = role;
    }

    @Override
    public void authenticate (AuthenticationFlowContext context) {
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
        role.roleSetting(context);
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        final String tomsId = formData.get("tomsId").get(0);
        var user = context.getUser();
        user.setAttribute("toms_id", Collections.singletonList(tomsId));
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
