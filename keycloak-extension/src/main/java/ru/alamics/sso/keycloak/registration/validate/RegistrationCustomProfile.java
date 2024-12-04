package ru.alamics.sso.keycloak.registration.validate;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.Config;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.authentication.forms.RegistrationPage;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;

import jakarta.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;

import static ru.alamics.sso.registration.model.FormConstants.FIELD_FIRST_NAME;
import static ru.alamics.sso.registration.model.FormConstants.FIELD_PHONE;

@Slf4j
public class RegistrationCustomProfile implements FormAction, FormActionFactory {
    private static final String PROVIDER_ID = "registration-profile-action";

    private static final String DISPLAY_NAME = "Profile Custom Validation";

    @Override
    public String getDisplayType() { return DISPLAY_NAME; }

    @Override
    public String getReferenceCategory() { return null; }

    @Override
    public boolean isConfigurable() { return false; }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() { return REQUIREMENT_CHOICES; }

    @Override
    public boolean isUserSetupAllowed() { return false; }

    @Override
    public void buildPage(FormContext context, LoginFormsProvider form) {}

    @Override
    public void validate(ValidationContext context) {
        log.info("Profile Custom Validation");
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        List<FormMessage> errors = new ArrayList<>();

        context.getEvent().detail(Details.REGISTER_METHOD, "form");
        String eventError = Errors.INVALID_REGISTRATION;

        // empty is ok
        /*
        if (Validation.isBlank(formData.getFirst((RegistrationPage.FIELD_FIRST_NAME)))) {
            errors.add(new FormMessage(RegistrationPage.FIELD_FIRST_NAME, Messages.MISSING_FIRST_NAME));
        }
         */

        // empty is ok
        /*
        if (Validation.isBlank(formData.getFirst((RegistrationPage.FIELD_LAST_NAME)))) {
            errors.add(new FormMessage(RegistrationPage.FIELD_LAST_NAME, Messages.MISSING_LAST_NAME));
        }
        */
        formData.remove(RegistrationPage.FIELD_LAST_NAME);
        formData.add(RegistrationPage.FIELD_LAST_NAME, " ");
        formData.remove(FIELD_FIRST_NAME);
        formData.add(FIELD_FIRST_NAME, formData.getFirst(FIELD_PHONE));

        String email = formData.getFirst(Validation.FIELD_EMAIL);
        boolean emailValid = true;
        if (Validation.isBlank(email)) {
            errors.add(new FormMessage(RegistrationPage.FIELD_EMAIL, Messages.MISSING_EMAIL));
          //  context.getAuthenticationSession().setAuthNote("email_error", "email is missing");
            emailValid = false;
        } else if (!Validation.isEmailValid(email)) {
            context.getEvent().detail(Details.EMAIL, email);
            errors.add(new FormMessage(RegistrationPage.FIELD_EMAIL, Messages.INVALID_EMAIL));
           // context.getAuthenticationSession().setAuthNote("email_error", "email is not valid");
            emailValid = false;
        }

        if (emailValid && !context.getRealm().isDuplicateEmailsAllowed() && context.getSession().users().getUserByEmail(context.getRealm(), email) != null) {
            eventError = Errors.EMAIL_IN_USE;
            formData.remove(Validation.FIELD_EMAIL);
            context.getEvent().detail(Details.EMAIL, email);
         //   context.getAuthenticationSession().setAuthNote("dupl_email", "email already exists");
            errors.add(new FormMessage(RegistrationPage.FIELD_EMAIL, Messages.EMAIL_EXISTS));
        }

        if (errors.size() > 0) {
            context.error(eventError);
            context.validationError(formData, errors);
        } else {
            context.success();
        }
    }

    @Override
    public void success(FormContext context) {
        UserModel user = context.getUser();
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        user.setFirstName(formData.getFirst(RegistrationPage.FIELD_FIRST_NAME));
        user.setLastName(formData.getFirst(RegistrationPage.FIELD_LAST_NAME));
        user.setEmail(formData.getFirst(RegistrationPage.FIELD_EMAIL));
    }

    @Override
    public boolean requiresUser() { return false; }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) { return true; }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {}

    @Override
    public String getHelpText() {
        return "Validates email, first name, and last name attributes and stores them in user data.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() { return List.of(); }

    @Override
    public FormAction create(KeycloakSession session) { return this; }

    @Override
    public void init(Config.Scope config) {}

    @Override
    public void postInit(KeycloakSessionFactory factory) {}

    @Override
    public void close() {}

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
