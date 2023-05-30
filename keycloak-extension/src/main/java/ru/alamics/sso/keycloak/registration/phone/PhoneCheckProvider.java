package ru.alamics.sso.keycloak.registration.phone;

import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.models.utils.FormMessage;
import ru.alamics.sso.keycloak.registration.mapper.UserModelUserMapper;
import ru.alamics.sso.registration.model.MessageConstants;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.registration.service.UserFindService;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;

import static ru.alamics.sso.registration.model.FormConstants.FIELD_EMAIL;
import static ru.alamics.sso.registration.model.FormConstants.FIELD_PHONE;

public class PhoneCheckProvider implements FormAction {

    private final UserFindService userFindService;

    public PhoneCheckProvider(UserFindService userFindService) {
        this.userFindService = userFindService;
    }

    @Override
    public void buildPage(FormContext context, LoginFormsProvider form) {

    }

    @Override
    public void validate(ValidationContext context) {

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        List<FormMessage> errors = new ArrayList<>();

        context.getEvent().detail(Details.REGISTER_METHOD, "form");
        String eventError = Errors.INVALID_REGISTRATION;


        User user = User.builder()
                .email(formData.getFirst(FIELD_EMAIL))
                .phone(formData.getFirst(FIELD_PHONE))
                .build();

        if (user.getPhone() == null || user.getPhone().isEmpty()) {
            formData.remove(FIELD_PHONE);
            context.getEvent().detail("Phone", user.getPhone());
            errors.add(new FormMessage(FIELD_PHONE, "Телефон должен быть заполнен"));
            context.getAuthenticationSession().setAuthNote("phone_error", "phone is missing");
        } else {
            UserEntity userEntity = userFindService.getUserByPhone(context.getRealm(), user.getPhone());

            if (userEntity != null) {
                formData.remove(FIELD_PHONE);
                context.getEvent().detail("Phone", user.getPhone());
                errors.add(new FormMessage(FIELD_PHONE, MessageConstants.PHONE_EXISTS));
                context.getAuthenticationSession().setAuthNote("dupl_phone", "phone already exists");
            }
        }

        if (formData.containsKey("grant_type") && !user.getPhone().matches("^\\d+$")) {
            context.getAuthenticationSession().setAuthNote("phone_error", "phone is not valid");
            errors.add(new FormMessage(FIELD_PHONE, MessageConstants.PHONE_INVALID));
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
        UserModelUserMapper.fillAttributesFromContext(context.getUser(), context.getHttpRequest());
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
    public void close() {

    }
}
