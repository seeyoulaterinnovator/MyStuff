package ru.alamics.sso.keycloak.auth.form.new_auth.new_rest.reg;

import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.Config;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.authentication.forms.RegistrationPage;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventType;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;
import org.keycloak.services.resources.AttributeFormDataProcessor;
import ru.alamics.sso.jpa.entity.BrandEntity;
import ru.alamics.sso.jpa.repository.BrandRepository;
import ru.alamics.sso.keycloak.lookup.Lookup;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RestRegistrationUserCreation implements FormAction, FormActionFactory {

    public static final String PROVIDER_ID = "rest-registration-user-creation";

    @Override
    public void validate(ValidationContext context) {
        log.info("Rest Reg");

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        List<FormMessage> errors = new ArrayList<>();
        context.getEvent().detail(Details.REGISTER_METHOD, "form");

        String email = formData.getFirst(Validation.FIELD_EMAIL);
        String username = formData.getFirst(RegistrationPage.FIELD_USERNAME);
        context.getEvent().detail(Details.USERNAME, username);
        context.getEvent().detail(Details.EMAIL, email);

        String usernameField = RegistrationPage.FIELD_USERNAME;
        if (context.getRealm().isRegistrationEmailAsUsername()) {
            context.getEvent().detail(Details.USERNAME, email);

            if (Validation.isBlank(email)) {
                errors.add(new FormMessage(RegistrationPage.FIELD_EMAIL, Messages.MISSING_EMAIL));
                context.getAuthenticationSession().setAuthNote("email_error", "email is missing");
            } else if (!Validation.isEmailValid(email)) {
                errors.add(new FormMessage(RegistrationPage.FIELD_EMAIL, Messages.INVALID_EMAIL));
                context.getAuthenticationSession().setAuthNote("email_error", "email is not valid");
                formData.remove(Validation.FIELD_EMAIL);
            }
            if (!errors.isEmpty()) {
                context.error(Errors.INVALID_REGISTRATION);
                context.validationError(formData, errors);
                return;
            }
            if (email != null && !context.getRealm().isDuplicateEmailsAllowed() && context.getSession().users().getUserByEmail(context.getRealm(), email) != null) {
                context.error(Errors.EMAIL_IN_USE);
                formData.remove(Validation.FIELD_EMAIL);
                errors.add(new FormMessage(RegistrationPage.FIELD_EMAIL, Messages.EMAIL_EXISTS));
                context.validationError(formData, errors);
                context.getAuthenticationSession().setAuthNote("dupl_email", "email already exists");
                return;
            }
        } else {
            if (Validation.isBlank(username)) {
                context.error(Errors.INVALID_REGISTRATION);
                errors.add(new FormMessage(RegistrationPage.FIELD_USERNAME, Messages.MISSING_USERNAME));
                context.validationError(formData, errors);
                return;
            }

            if (context.getSession().users().getUserByUsername(context.getRealm(), username) != null) {
                context.error(Errors.USERNAME_IN_USE);
                errors.add(new FormMessage(usernameField, Messages.USERNAME_EXISTS));
                formData.remove(Validation.FIELD_USERNAME);
                context.validationError(formData, errors);
                return;
            }

        }
        context.success();
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }


    @Override
    public void close() {

    }

    @Override
    public String getDisplayType() {
        return "REST Registration User Creation";
    }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
    };

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public FormAction create(KeycloakSession session) {
        return this;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getHelpText() {
        return "This action must always be first! Validates the username of the user in validation phase.  In success phase, this will create the user in the database.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return null;
    }

    @Override
    public void buildPage(FormContext context, LoginFormsProvider form) {

    }

    @Override
    public void success(FormContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        if (!formData.containsKey("grant_type") && !formData.getFirst("grant_type").equals("password")) {
            return;
        }
        String email = formData.getFirst(Validation.FIELD_EMAIL);
        String username = formData.getFirst(RegistrationPage.FIELD_USERNAME);
        if (context.getRealm().isRegistrationEmailAsUsername()) {
            username = formData.getFirst(RegistrationPage.FIELD_EMAIL);
        }
        context.getEvent().detail(Details.USERNAME, username)
                .detail(Details.REGISTER_METHOD, "form")
                .detail(Details.EMAIL, email)
        ;
        UserModel user = context.getSession().users().addUser(context.getRealm(), username);
        user.setEnabled(true);

        user.setEmail(email);

        String brandId = formData.getFirst("markBrandId");
        if (brandId == null || brandId.isBlank()) {
            brandId = Lookup.lookup(BrandRepository.class)
                    .findDefaultByRealm(context.getRealm().getId())
                    .map(BrandEntity::getId)
                    .orElse(null);
        }
        if (brandId != null) {
            user.setSingleAttribute("markBrandId", brandId);
        }
        context.getAuthenticationSession().setClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM, username);
        AttributeFormDataProcessor.process(formData, context.getRealm(), user);
        context.setUser(user);
        context.getEvent().user(user);
        context.getEvent().success();
        context.newEvent().event(EventType.LOGIN);
        context.getEvent().client(context.getAuthenticationSession().getClient().getClientId())
                .detail(Details.REDIRECT_URI, context.getAuthenticationSession().getRedirectUri())
                .detail(Details.AUTH_METHOD, context.getAuthenticationSession().getProtocol());
        String authType = context.getAuthenticationSession().getAuthNote(Details.AUTH_TYPE);
        if (authType != null) {
            context.getEvent().detail(Details.AUTH_TYPE, authType);
        }
    }
}
