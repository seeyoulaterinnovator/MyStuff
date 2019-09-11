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
import org.keycloak.models.utils.FormMessage;
import ru.alamics.sso.keycloak.registration.mapper.UserModelUserMapper;
import ru.alamics.sso.registration.model.User;

import javax.persistence.EntityManager;
import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;

import static ru.alamics.sso.registration.model.FormConstants.FIELD_EMAIL;
import static ru.alamics.sso.registration.model.FormConstants.FIELD_PHONE;
import static ru.alamics.sso.registration.model.UserConstants.ATTR_PHONE_NAME;

public class PhoneCheckProvider implements FormAction {

    private final EntityManager em;

    public PhoneCheckProvider(EntityManager em) {
        this.em = em;
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

        if (user.getPhone() == null || user.getPhone().isBlank()) {
            formData.remove(FIELD_PHONE);
            context.getEvent().detail("Phone", user.getPhone());
            errors.add(new FormMessage(FIELD_PHONE, "Телефон должен быть заполнен"));
        } else {
            Long count = em.createQuery("select count(u.id) from UserAttributeEntity u " +
                    "where u.name = :ph_attr_name and u.value like '%' || :phone || '%'", Long.class) // TODO =
                    .setParameter("ph_attr_name", ATTR_PHONE_NAME)
                    .setParameter("phone", user.getPhone())
                    .getSingleResult();

            if (count > 0) {
                formData.remove(FIELD_PHONE);
                context.getEvent().detail("Phone", user.getPhone());
                errors.add(new FormMessage(FIELD_PHONE, "Номер мобильного телефона уже используется в другой учетной записи. Если вы уже регистрировались, попробуйте войти в свою учетную запись, либо укажите другой номер телефона."));
            }
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
