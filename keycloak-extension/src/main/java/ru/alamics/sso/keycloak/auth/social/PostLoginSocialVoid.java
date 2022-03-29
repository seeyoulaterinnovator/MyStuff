package ru.alamics.sso.keycloak.auth.social;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import ru.alamics.sso.keycloak.auth.AbstractAuthenticatorFactory;
import ru.alamics.sso.util.Util;

import static ru.alamics.sso.registration.model.UserConstants.AUTH_FORM_SUCCESS;

public class PostLoginSocialVoid extends AbstractAuthenticatorFactory implements Authenticator {

    private static final String PROVIDER_ID = "post-login-social-void";
    private static final String DISPLAY_NAME = "Post Login Social Void";
    private static final String HELP_TEXT = "";

    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
    };

    @Override
    public String getDisplayType() {
        return DISPLAY_NAME;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public String getHelpText() {
        return HELP_TEXT;
    }

    @Override
    public Authenticator create(KeycloakSession session) {

        return this;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        context.getAuthenticationSession().setAuthNote(AUTH_FORM_SUCCESS, Util.TRUE_STR);
        context.success();
    }

    @Override
    public void action(AuthenticationFlowContext context) {

    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return false;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }
}
