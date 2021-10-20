package ru.alamics.sso.keycloak.auth;

import lombok.SneakyThrows;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.authentication.authenticators.directgrant.AbstractDirectGrantAuthenticator;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.models.*;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.managers.AuthenticationManager;
import ru.alamics.sso.registration.service.UserFindService;
import ru.alamics.sso.util.Util;

import javax.naming.InitialContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.List;

public class ValidateUsernameOrPhone extends AbstractDirectGrantAuthenticator {

    public static final String PROVIDER_ID = "direct-grant-validate-mail-or-phone";
    public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED
    };

    @SneakyThrows
    @Override
    public void authenticate(AuthenticationFlowContext context) {
        String username = retrieveUsername(context);
        if (username == null) {
            context.getEvent().error(Errors.USER_NOT_FOUND);
            Response challengeResponse = errorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), "invalid_request", "Missing parameter: username");
            context.failure(AuthenticationFlowError.INVALID_USER, challengeResponse);
            return;
        }
        context.getEvent().detail(Details.USERNAME, username);
        context.getAuthenticationSession().setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, username);

        UserModel user = null;
        try {
            if (username.startsWith("+7")) {
                InitialContext initialContext = new InitialContext();
                UserFindService userFindService = (UserFindService) initialContext.lookup("java:global/domru-sso/" + UserFindService.class.getSimpleName());
                user = Util.getUserAdapter(context.getSession(), userFindService.getUserByPhone(context.getRealm(), username));
            } else {
                user = KeycloakModelUtils.findUserByNameOrEmail(context.getSession(), context.getRealm(), username);

            }
        } catch (ModelDuplicateException mde) {
            ServicesLogger.LOGGER.modelDuplicateException(mde);
            Response challengeResponse = errorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), "invalid_request", "Invalid user credentials");
            context.failure(AuthenticationFlowError.INVALID_USER, challengeResponse);
            return;
        }


        if (user == null) {
            context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);
            Response challengeResponse = errorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), "invalid_grant", "Invalid user credentials");
            context.failure(AuthenticationFlowError.INVALID_USER, challengeResponse);
            return;
        }
        if (!user.isEnabled()) {
            context.getEvent().user(user);
            context.getEvent().error(Errors.USER_DISABLED);
            Response challengeResponse = errorResponse(Response.Status.BAD_REQUEST.getStatusCode(), "invalid_grant", "Account disabled");
            context.failure(AuthenticationFlowError.INVALID_USER, challengeResponse);
            return;
        }
        if (context.getRealm().isBruteForceProtected()) {
            if (context.getProtector().isTemporarilyDisabled(context.getSession(), context.getRealm(), user)) {
                context.getEvent().user(user);
                context.getEvent().error(Errors.USER_TEMPORARILY_DISABLED);
                Response challengeResponse = errorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), "invalid_grant", "Invalid user credentials");
                context.failure(AuthenticationFlowError.INVALID_USER, challengeResponse);
                return;
            }
        }
        context.setUser(user);
        context.success();
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

    @Override
    public String getDisplayType() {
        return "UsernameOrPhone Validation";
    }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Validates the Username Or Phone supplied as a 'username' form parameter in direct grant request";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return null;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    protected String retrieveUsername(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> inputData = context.getHttpRequest().getDecodedFormParameters();
        return inputData.getFirst(AuthenticationManager.FORM_USERNAME);
    }
}
