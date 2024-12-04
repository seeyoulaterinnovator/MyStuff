package ru.alamics.sso.keycloak.auth.requiredactions;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.requiredactions.UpdateProfile;
import org.keycloak.events.Details;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.messages.Messages;
import ru.alamics.sso.keycloak.lookup.Lookup;
import org.keycloak.services.resources.AttributeFormDataProcessor;
import ru.alamics.sso.registration.model.MessageConstants;
import ru.alamics.sso.registration.service.UserFindService;
import ru.alamics.sso.util.Util;

import java.util.Collections;
import java.util.List;

import static ru.alamics.sso.registration.model.UserConstants.ATTR_PHONE_NAME;

@Slf4j
public class SsoUpdateProfile extends UpdateProfile {

    private final static String DEFAULT_USER_LASTNAME = " ";

    @Override
    public void processAction(RequiredActionContext context) {
        EventBuilder event = context.getEvent();
        event.event(EventType.UPDATE_PROFILE);
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        UserModel user = context.getUser();
        KeycloakSession session = context.getSession();
        RealmModel realm = context.getRealm();

        List<FormMessage> errors = SsoFormValidation.validateUpdateProfileForm(realm, formData);
        if (!errors.isEmpty()) {
            Response challenge = context.form()
                    .setErrors(errors)
                    .setFormData(formData)
                    .createResponse(UserModel.RequiredAction.UPDATE_PROFILE);
            context.challenge(challenge);
            return;
        }

        String email = formData.getFirst("email");
        String phone = formData.getFirst("phone");
        String firstName = formData.getFirst("firstName");

        user.setLastName(DEFAULT_USER_LASTNAME);

        String oldFirstName = user.getFirstName();
        String oldEmail = user.getEmail();

        String oldPhone = user.getFirstAttribute(ATTR_PHONE_NAME);
        user.setAttribute(ATTR_PHONE_NAME, Collections.singletonList(phone));

        boolean emailChanged = !(Util.isEmpty(oldEmail) || oldEmail.equals(email));
        boolean phoneChanged = !(Util.isEmpty(oldPhone) || oldPhone.equals(phone));
        boolean firstNameChanged = !(Util.isEmpty(oldFirstName) || oldFirstName.equals(firstName));

        if (firstNameChanged) {
            user.setFirstName(firstName);
        }

        final UserFindService userFindService = Lookup.lookup(UserFindService.class);

        if (userFindService == null) {
            log.error("UserFindService failed lookup");
            return;
        }

        if (phoneChanged) {
            if (userFindService.getUserByPhone(context.getRealm(), phone) != null) {
                formData.remove("phone");
                Response challenge = context.form()
                        .setError(MessageConstants.PHONE_EXISTS)
                        .setFormData(formData)
                        .createResponse(UserModel.RequiredAction.UPDATE_PROFILE);
                context.challenge(challenge);
                return;
            }
            user.setAttribute(ATTR_PHONE_NAME, Collections.singletonList(phone));
        }

        if (emailChanged) {
            UserModel userByEmail = session.users().getUserByEmail(realm, email);
            // check for duplicated email
            if (userByEmail != null && !userByEmail.getId().equals(user.getId())) {
                Response challenge = context.form()
                        .setError(Messages.EMAIL_EXISTS)
                        .setFormData(formData)
                        .createResponse(UserModel.RequiredAction.UPDATE_PROFILE);
                context.challenge(challenge);
                return;
            }

            user.setUsername(email);
            user.setEmail(email);
            user.setEmailVerified(false);
        }

        AttributeFormDataProcessor.process(formData, context.getRealm(), user);

        if (emailChanged) {
            event.clone().event(EventType.UPDATE_EMAIL).detail(Details.PREVIOUS_EMAIL, oldEmail).detail(Details.UPDATED_EMAIL, email).success();
        }

        context.success();
    }
}
