package ru.alamics.sso.keycloak.auth.form.new_auth;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.events.Details;
import org.keycloak.events.EventType;
import org.keycloak.models.RequiredActionProviderModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.alamics.sso.auth_n_regi.AuthOrRegTypeNotFoundException;
import ru.alamics.sso.jpa.entity.auth_reg.AuthOrRegType;
import ru.alamics.sso.keycloak.auth.form.new_auth.common_mail_sender.EmailSenderService;

import java.util.UUID;

public interface SsoUtil {


    static void sendEmailVer(RequiredActionContext context) {
        if (!context.getUser().isEmailVerified()) {
            EmailSenderService.sendVerifyEmail(context.getSession(),
                    context.form(),
                    context.getUser(),
                    context.getAuthenticationSession(),
                    context.getEvent().clone().event(EventType.SEND_VERIFY_EMAIL).detail(Details.EMAIL, context.getUser().getEmail()));
        }
    }

    static String generatePattern() {
        String uuid1 = UUID.randomUUID().toString();
        String uuid2 = UUID.randomUUID().toString();
        String randomString = generateRandomString(11);
        return uuid1 + "." + randomString + "." + uuid2;
    }

    static String generateRandomString(int length) {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }

    static boolean addRequiredAction(AuthenticationFlowContext context, String providerName, UserModel userModel) {
        RequiredActionProviderModel providerModel = context.getRealm().getRequiredActionProviderByAlias(providerName);

        if (providerModel.isEnabled() && !userModel.getRequiredActions().contains(providerName)) {
            userModel.addRequiredAction(providerName);
            return true;
        }
        return false;
    }

    static int getAuthOrRegType(AuthenticationSessionModel authenticationSessionModel) throws AuthOrRegTypeNotFoundException{
        AuthOrRegType[] authOrRegTypes = AuthOrRegType.values();

        for (AuthOrRegType authOrRegType : authOrRegTypes) {
            if (authenticationSessionModel.getAuthNote(authOrRegType.getButtonName()) != null) {
                return authOrRegType.getId();
            }
        }
        throw new AuthOrRegTypeNotFoundException("Auth or Reg Type Not Found");
    }
}
