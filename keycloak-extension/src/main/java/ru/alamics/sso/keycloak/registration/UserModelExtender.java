package ru.alamics.sso.keycloak.registration;

import org.keycloak.Config;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.provider.ProviderConfigProperty;
import ru.alamics.sso.keycloak.registration.mapper.UserModelUserMapper;

import ru.alamics.sso.registration.TbapiService;
import ru.alamics.sso.registration.UserExtension;
import ru.alamics.sso.registration.model.TbapiConnectConfig;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.remote.tbapi.TbapiServiceRestImpl;

import java.util.List;
import java.util.Map;

public class UserModelExtender implements FormAction, FormActionFactory {

    private static final String PROVIDER_ID = "registration-user-extension";

    private static final String HOSTNAME_PROPERTY_NAME = "targetHost";
    private static final String HOSTNAME_PROPERTY_LABEL = "Адрес";
    private static final String HOSTNAME_PROPERTY_HELP_TEXT = "Домен, к которому будут выполняться запросы";

    private static final String PORT_PROPERTY_NAME = "targetPort";
    private static final String PORT_PROPERTY_LABEL = "Порт";
    private static final String PORT_PROPERTY_HELP_TEXT = "Порт, на котором сервер слушает запросы";

    private static final String AUTH_APPNAME_NAME = "authAppname";
    private static final String AUTH_APPNAME_LABEL = "Appname";
    private static final String AUTH_APPNAME_HELP_TEXT = "Параметр appname заголовка авторизации";

    private static final String AUTH_USERNAME_NAME = "authUsername";
    private static final String AUTH_USERNAME_LABEL = "Username";
    private static final String AUTH_USERNAME_HELP_TEXT = "Параметр username заголовка авторизации";

    private static final String PATH_PROPERTY_NAME = "targetPath";
    private static final String PATH_PROPERTY_LABEL = "Путь";
    private static final String PATH_PROPERTY_HELP_TEXT = "Путь, к которому нужно выполнить запрос";

    private static final String SCHEMA_PROPERTY_NAME = "targetSchema";
    private static final String SCHEMA_PROPERTY_LABEL = "HTTPS";
    private static final String SCHEMA_PROPERTY_HELP_TEXT = "Использовать ли шифрованное подключение";

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
                    ProviderConfigProperty.STRING_TYPE, "/api/v1/leadManagement/lead"),
            new ProviderConfigProperty(SCHEMA_PROPERTY_NAME, SCHEMA_PROPERTY_LABEL, SCHEMA_PROPERTY_HELP_TEXT,
                    ProviderConfigProperty.BOOLEAN_TYPE, false)
    );


    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
    };
    private final TbapiService tbapiService;
    private final UserExtension userExtension;
    private final UserModelUserMapper mapper;

    public UserModelExtender() {
        tbapiService = new TbapiService(new TbapiServiceRestImpl());
        userExtension = new UserExtension();
        mapper = new UserModelUserMapper();
    }

    @Override
    public void buildPage(FormContext context, LoginFormsProvider form) {
        // nothing to do here
    }

    @Override
    public void validate(ValidationContext context) {
        context.success();
    }

    @Override
    public void success(FormContext context) {
        UserModel model = context.getUser();

        Map<String, String> config = context.getAuthenticatorConfig().getConfig();

        TbapiConnectConfig conectConfig = new TbapiConnectConfig();

        conectConfig.setHost(config.get(HOSTNAME_PROPERTY_NAME));
        conectConfig.setPort(Integer.parseInt(config.get(PORT_PROPERTY_NAME)));
        conectConfig.setAppname(config.get(AUTH_APPNAME_NAME));
        conectConfig.setUsername(config.get(AUTH_USERNAME_NAME));
        conectConfig.setPath(config.get(PATH_PROPERTY_NAME));
        conectConfig.setSecure(Boolean.parseBoolean(config.get(SCHEMA_PROPERTY_NAME)));

        User user = mapper.mapToUser(model);

        Map<String, Object> attributes = tbapiService.registerUser(user, conectConfig);
        userExtension.extendUser(user, attributes);

        mapper.mergeUserInto(user, model);

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
