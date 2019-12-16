package ru.alamics.sso.keycloak.registration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.provider.ProviderConfigProperty;
import ru.alamics.sso.keycloak.registration.mapper.UserModelUserMapper;

import ru.alamics.sso.registration.tbapi.TbapiService;
import ru.alamics.sso.registration.UserExtension;
import ru.alamics.sso.registration.tbapi.model.TbapiConnectConfig;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.remote.tbapi.TbapiServiceRestImpl;

import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static ru.alamics.sso.keycloak.registration.UserConfigProperties.*;
import static ru.alamics.sso.registration.model.FormConstants.*;
import static ru.alamics.sso.registration.model.UserConstants.ATTR_ORG_NAME;

public class UserModelExtender implements FormAction, FormActionFactory {

    private static final Logger log = Logger.getLogger(UserModelExtender.class);

    private static final String TBAPI_CHECK_DATA = "tbapi_check_data";

    // jackson serialize
    ObjectMapper jacksonMapper = new ObjectMapper();

    private static final String PROVIDER_ID = "registration-user-extension";

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = List.of(
            new ProviderConfigProperty(HOSTNAME_PROPERTY_NAME, HOSTNAME_PROPERTY_LABEL, HOSTNAME_PROPERTY_HELP_TEXT,
                    ProviderConfigProperty.STRING_TYPE, "localhost"),
            new ProviderConfigProperty(PORT_PROPERTY_NAME, PORT_PROPERTY_LABEL, PORT_PROPERTY_HELP_TEXT,
                    ProviderConfigProperty.STRING_TYPE, 80),
            new ProviderConfigProperty(AUTH_APPNAME_NAME, AUTH_APPNAME_LABEL, AUTH_APPNAME_HELP_TEXT,
                    ProviderConfigProperty.STRING_TYPE, "appname"),
            new ProviderConfigProperty(AUTH_USERNAME_NAME, AUTH_USERNAME_LABEL, AUTH_USERNAME_HELP_TEXT,
                    ProviderConfigProperty.STRING_TYPE, "username"),
            new ProviderConfigProperty(PATH_PROPERTY_NAME, PATH_PROPERTY_LABEL, PATH_PROPERTY_HELP_TEXT,
                    ProviderConfigProperty.STRING_TYPE, "/api/v1/customerManagement/customerAccount"),
            new ProviderConfigProperty(SCHEMA_PROPERTY_NAME, SCHEMA_PROPERTY_LABEL, SCHEMA_PROPERTY_HELP_TEXT,
                    ProviderConfigProperty.BOOLEAN_TYPE, false)
    );


    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
    };
    private final TbapiService tbapiService;
    private final UserExtension userExtension;

    public UserModelExtender() {
        tbapiService = new TbapiService(new TbapiServiceRestImpl());
        userExtension = new UserExtension();
    }

    @Override
    public void buildPage(FormContext context, LoginFormsProvider form) {
        // nothing to do here
    }

    @Override
    public void validate(ValidationContext context) {

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        List<FormMessage> errors = new ArrayList<>();
        String eventError = Errors.INVALID_REGISTRATION;

        try {
            context.getEvent().detail(Details.REGISTER_METHOD, "form");


            Map<String, String> config = context.getAuthenticatorConfig().getConfig();

            TbapiConnectConfig connectConfig = new TbapiConnectConfig();

            connectConfig.setHost(config.get(HOSTNAME_PROPERTY_NAME));
            connectConfig.setPort(Integer.parseInt(config.get(PORT_PROPERTY_NAME)));
            connectConfig.setAppname(config.get(AUTH_APPNAME_NAME));
            connectConfig.setUsername(config.get(AUTH_USERNAME_NAME));
            connectConfig.setPath(config.get(PATH_PROPERTY_NAME));
            connectConfig.setSecure(Boolean.parseBoolean(config.get(SCHEMA_PROPERTY_NAME)));


            User user = User.builder()
                    .name(formData.getFirst(FIELD_FIRST_NAME))
                    .email(formData.getFirst(FIELD_EMAIL))
                    .phone(formData.getFirst(FIELD_PHONE))
                    .build();

            String orgName = formData.getFirst(FIELD_ORG_NAME);
            // TODO на стандартной верстке нет поля организации
            if (orgName == null) {
                orgName = formData.getFirst(FIELD_LAST_NAME);
            }
            user.getAttributes().put(ATTR_ORG_NAME, Collections.singletonList(orgName));


            Map<String, Object> attributes = tbapiService.registerUser(user, connectConfig);

            userExtension.extendUser(user, attributes);


            String userStr = null;
            try {
                userStr = jacksonMapper.writer().writeValueAsString(user);
                log.info(String.format("serialized: %s", userStr));
            } catch (Exception e) {
                log.error("", e);
            }

            if (userStr != null)
                context.getAuthenticationSession().setAuthNote(TBAPI_CHECK_DATA, userStr);

        } catch (Exception e) {
            log.error("", e);
            errors.add(new FormMessage("Регистрация временно недоступна, попробуйте повторить попытку позже"));
        }

        if (!errors.isEmpty()) {
            context.error(eventError);
            context.validationError(formData, errors);

        } else {
            context.success();
        }
    }

    @Override
    public void success(FormContext context) {

        UserModel model = context.getUser();

        String userStr = context.getAuthenticationSession().getAuthNote(TBAPI_CHECK_DATA);

        User userNewData = null;
        try {
            userNewData = jacksonMapper.readValue(userStr, User.class);
        } catch (IOException e) {
            log.error("Error while serializing", e);
        }

        if (userNewData != null)
            UserModelUserMapper.mergeUserInto(userNewData, model);
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
        // nothing to do here
    }

    @Override
    public void close() {
        // nothing to do here
    }

    @Override
    public String getDisplayType() {
        return "Registration user extension";
    }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override
    public boolean isConfigurable() {
        return true;
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
        return "Common help text";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    @Override
    public FormAction create(KeycloakSession session) {
        return this;
    }

    @Override
    public void init(Config.Scope config) {
        // nothing to do here
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // nothing to do here
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
