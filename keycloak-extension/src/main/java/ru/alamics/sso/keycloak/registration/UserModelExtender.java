package ru.alamics.sso.keycloak.registration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.MultivaluedMap;
import org.jboss.logging.Logger;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.provider.ProviderConfigProperty;
import ru.alamics.sso.keycloak.registration.mapper.UserModelUserMapper;
import ru.alamics.sso.keycloak.registration.rias.RiasCheckProvider;
import ru.alamics.sso.registration.UserExtension;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.registration.tbapi.TbapiService;
import ru.alamics.sso.registration.tbapi.model.TbapiConnect;
import ru.alamics.sso.registration.tbapi.model.TbapiConnectConfig;
import ru.alamics.sso.remote.tbapi.TbapiServiceRestImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static ru.alamics.sso.registration.model.FormConstants.FIELD_EMAIL;
import static ru.alamics.sso.registration.model.FormConstants.FIELD_PHONE;

public class UserModelExtender extends AbstractFormActionFactory implements FormAction {

    private static final Logger log = Logger.getLogger(UserModelExtender.class);

    private static final String TBAPI_CHECK_DATA = "tbapi_check_data";

    private static final String PROVIDER_ID = "registration-user-extension";
    private static final String DISPLAY_NAME = "Registration user extension";
    private static final String HELP_TEXT = "Common help text";

    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
    };
    private final TbapiService tbapiService;
    private final UserExtension userExtension;
    // jackson serialize
    ObjectMapper jacksonMapper = new ObjectMapper();

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
        log.info("Registration user extension");

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        List<FormMessage> errors = new ArrayList<>();
        String eventError = Errors.INVALID_REGISTRATION;

        if (context.getAuthenticationSession().getAuthNote(RiasCheckProvider.RIAS_REJECTED) != null) {
            log.info("RIAS rejected, so no need to request TBAPI");

            // иначе падает разбор на стороне КС
            context.error(eventError);
            context.validationError(formData, errors);
            return;
        }

        try {
            context.getEvent().detail(Details.REGISTER_METHOD, "form");

            User user = User.builder()
                    .name(formData.getFirst(FIELD_PHONE))
                    .email(formData.getFirst(FIELD_EMAIL))
                    .phone(formData.getFirst(FIELD_PHONE))
                    .build();

            Map<String, Object> attributes = tbapiService.registerUser(user, new TbapiConnectConfig(TbapiConnect.REGISTRATION));

            log.info(String.format("registerUser attributes: %s", attributes));

            userExtension.extendUser(user, attributes);


            String userStr = null;
            try {
                userStr = jacksonMapper.writer().writeValueAsString(user);
                log.info(String.format("serialized user: %s", userStr));
            } catch (Exception e) {
                log.error("", e);
            }

            if (userStr != null)
                context.getAuthenticationSession().setAuthNote(TBAPI_CHECK_DATA, userStr);

        } catch (Exception e) {
            log.error("", e);
            errors.add(new FormMessage("Регистрация временно недоступна, попробуйте повторить попытку позже"));
            context.getAuthenticationSession().setAuthNote("reg_error", "registration is not available");
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
    public List<ProviderConfigProperty> getConfigProperties() {
        return Arrays.asList();
    }

    @Override
    public FormAction create(KeycloakSession session) {
        log.info("Creating UserModelExtender");
        return this;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
