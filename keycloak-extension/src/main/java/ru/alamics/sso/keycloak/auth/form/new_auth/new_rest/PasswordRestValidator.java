package ru.alamics.sso.keycloak.auth.form.new_auth.new_rest;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.directgrant.ValidatePassword;
import org.keycloak.events.Errors;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import ru.alamics.sso.keycloak.facade.CachedUserPostFacade;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.dto.UserPostResponse;

import javax.ws.rs.core.Response;
import java.util.List;

import static ru.alamics.sso.keycloak.auth.form.new_auth.new_rest.RestAuthHelper.*;


public class PasswordRestValidator extends ValidatePassword {

    private static final String PROVIDER_ID = "rest-password-validator";

    private static final String DISPLAY_NAME = "Rest Password Validator";

    private static final String HELP_TEXT = "";

    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED
    };

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        final CachedUserPostFacade cachedUserPostFacade = Lookup.lookup(CachedUserPostFacade.class);
        String password = retrievePassword(context);
        UserModel user = context.getUser();
        List<UserPostResponse> attributes;
        attributes = cachedUserPostFacade.findByUserId(context.getUser().getId());
        setEmailSetter(user);
        setPostSelector(user, attributes);

        if (password == null) {
            context.getAuthenticationSession().setAuthNote("restSecondPhase", "restSecondPhase");
            setSecondPhaseAuth(context, user);
            context.success();
            return;
        }
        boolean valid = context.getSession().userCredentialManager().isValid(context.getRealm(), context.getUser(), UserCredentialModel.password(password));
        if (!valid) {
            context.getEvent().user(context.getUser());
            context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);
            Response challengeResponse = errorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), "invalid_grant", "Invalid user credentials");
            context.failure(AuthenticationFlowError.INVALID_USER, challengeResponse);
            return;
        }

        context.success();
    }

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
    public String getId() {
        return PROVIDER_ID;
    }


}
