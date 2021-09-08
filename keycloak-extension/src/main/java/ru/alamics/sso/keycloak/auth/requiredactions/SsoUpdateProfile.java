package ru.alamics.sso.keycloak.auth.requiredactions;

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
import org.keycloak.services.resources.AttributeFormDataProcessor;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.List;

@Slf4j
public class SsoUpdateProfile extends UpdateProfile {

    private final static String DEFAULT_USER_LASTNAME = " ";

    @Override
    public void processAction(RequiredActionContext context) {
        super.processAction(context);
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

        String username = formData.getFirst("email");

        user.setFirstName(formData.getFirst("firstName"));
        user.setLastName(DEFAULT_USER_LASTNAME);

        String email = formData.getFirst("email");

        String oldEmail = user.getEmail();
        boolean emailChanged = oldEmail != null ? !oldEmail.equals(email) : email != null;

        if (emailChanged) {
            if (!realm.isDuplicateEmailsAllowed()) {
                UserModel userByEmail = session.users().getUserByEmail(email, realm);

                // check for duplicated email
                if (userByEmail != null && !userByEmail.getId().equals(user.getId())) {
                    Response challenge = context.form()
                            .setError(Messages.EMAIL_EXISTS)
                            .setFormData(formData)
                            .createResponse(UserModel.RequiredAction.UPDATE_PROFILE);
                    context.challenge(challenge);
                    return;
                }
            }

            if (session.users().getUserByUsername(username, realm) != null) {
                Response challenge = context.form()
                        .setError(Messages.USERNAME_EXISTS)
                        .setFormData(formData)
                        .createResponse(UserModel.RequiredAction.UPDATE_PROFILE);
                context.challenge(challenge);
                return;
            }

            user.setUsername(email);
            user.setEmail(email);
            user.setEmailVerified(false);
        }

        AttributeFormDataProcessor.process(formData, realm, user);

        if (emailChanged) {
            event.clone().event(EventType.UPDATE_EMAIL).detail(Details.PREVIOUS_EMAIL, oldEmail).detail(Details.UPDATED_EMAIL, email).success();
        }
        context.success();
    }
}
