package ru.alamics.sso.keycloak.resetcred;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.alamics.sso.jpa.entity.common.BlockType;
import ru.alamics.sso.keycloak.auth.AbstractAuthenticator;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.resetcred.factory.ResetFactory;
import ru.alamics.sso.keycloak.resetcred.factory.ResetFactoryImpl;
import ru.alamics.sso.keycloak.resetcred.type.ResetType;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.registration.model.FormConstants;
import ru.alamics.sso.registration.rias.exception.RiasCheckException;
import ru.alamics.sso.registration.rias.port.RiasApiService;
import ru.alamics.sso.registration.service.UserFindService;
import ru.alamics.sso.user.UserAttributeService;
import ru.alamics.sso.user.UserServiceUtil;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Objects;

import static ru.alamics.sso.keycloak.auth.form.new_auth.SsoUtil.sendEmailVer;


@Slf4j
public class ResetCredentialEmailOrPhone extends AbstractAuthenticator {

    private static final String RESET_CREDENTIALS_REDIRECT_URL = "reset.credentials.redirect.url";
    private final static String RESET_CRED_TO_RIAS_FORM = "reset-cred-to-rias.ftl";
    private final static String CLIENT_ID_B2B = "b2b";
    private final static String CLIENT_ID_DMP_KC_SIT = "dmp-kc-sit";

    private final KeycloakSession session;
    private final RiasApiService riasApiService;
    private final UserFindService userFindService;
    private final ApplicationProperties properties;
    private final UserAttributeService attributeService;

    ResetCredentialEmailOrPhone(KeycloakSession session) {
        this.session = session;

        riasApiService = Lookup.lookup(RiasApiService.class);
        log.info("Got riasService from context");

        userFindService = Lookup.lookup(UserFindService.class);
        log.info("Got userFindService from context");

        properties = Lookup.lookup(ApplicationProperties.class);
        attributeService = new UserAttributeService(session, userFindService);
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        log.info("authenticate reset start");
        UserModel user = context.getUser();
        ResetType resetType = ResetType.EMAIL;
        AuthenticationSessionModel authenticationSession = context.getAuthenticationSession();
        String username = authenticationSession.getAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME);

        UserEntity userFind = null;

        RealmModel realm = context.getRealm();

        if (user == null && realm.isLoginWithEmailAllowed() && username.contains("@")) {
            user = context.getSession().users().getUserByEmail(username, realm);
        }

        if (user == null && username.startsWith("+7")) {
            userFind = findUserByConvertUsernameToPhone(realm, username);
            if (userFind != null && userFind.getAttributes().stream().noneMatch(it -> it.getName().equals(BlockType.MANAGER_BLOCK.getType()))) {
                user = context.getSession().users().getUserById(userFind.getId(), context.getSession().realms().getRealm(userFind.getRealmId()));
                Objects.requireNonNull(user).setEnabled(true);
                username = userFind.getUsername();
                if(userFind.isEmailVerified()) {
                    authenticationSession.setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, userFind.getEmail());
                    context.getHttpRequest().getDecodedFormParameters().replace("username", Collections.singletonList(userFind.getEmail()));
                } else {
                    context.setUser(user);
                    log.info("authenticate reset-cred not Verified = {}", context.getUser().getEmail());
                    context.challenge(context.form().createForm("verify-email-by-reset.ftl"));
                }
            }
        }
        log.info("authenticate reset-cred = {}", user.getEmail());

        if (user == null && userFind == null && checkRias(context)) {
            return;
        }

        if (user == null && userFind == null && !checkRias(context)) {
            context.forkWithSuccessMessage(new FormMessage(Messages.EMAIL_SENT_ERROR));
            return;
        }

        if (userFind != null && Objects.requireNonNull(userFind).getAttributes()
                .stream().anyMatch(it -> it.getName().equals(BlockType.MANAGER_BLOCK.getType()))
                || user != null
                && !user.getAttribute(BlockType.MANAGER_BLOCK.getType()).isEmpty()) {

            context.forkWithErrorMessage(new FormMessage(Messages.ACCOUNT_DISABLED));
        } else {
            Objects.requireNonNull(user).setEnabled(true);
            attributeService.deleteAttributes(user.getId(), Collections.singletonList(BlockType.SYSTEM_BLOCK.getType()));
            context.forkWithSuccessMessage(new FormMessage(Messages.EMAIL_SENT));
        }

        authenticationSession.setAuthNote("RESET_TYPE", resetType.name());
        ResetFactory factory = new ResetFactoryImpl(this.session, context);
        ResetCredential resetCredential = factory.create(resetType);
        resetCredential.reset(user, username);

//        if (sendEmailVer(user, context.form(), context.getSession(), context.getAuthenticationSession(), context.getEvent())) {
//              resetCredential.reset(user, username);
//        }
    }

    private UserEntity findUserByConvertUsernameToPhone(RealmModel realm, final String username) {
        String phone = UserServiceUtil.doCleanPhoneStartWithSeven(username);

        return phone == null ? null : userFindService.getUserByPhone(realm, phone);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        AuthenticationSessionModel authenticationSession = context.getAuthenticationSession();
        log.info("action verify-email-by-reset context.getUser() = {}", context.getUser().getEmail());
        String username = authenticationSession.getAuthNote("email");
        log.info("action verify-email-by-reset username = {}", username);
        if(username.equals(context.getUser().getEmail())){
            context.getUser().setEmailVerified(true);
            authenticate(context);
            return;
        }
        context.forkWithErrorMessage(new FormMessage("Не получается отправить письмо. Учетная запись с такими данными не существует в системе"));
    }

    private boolean checkRias(AuthenticationFlowContext context) {
        AuthenticationSessionModel authenticationSession = context.getAuthenticationSession();
        String username = authenticationSession.getAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME);

        String clientId = context.getSession().getContext().getClient().getClientId();

        if (CLIENT_ID_B2B.equals(clientId) || CLIENT_ID_DMP_KC_SIT.equals(clientId)) {
            return false;
        }

        try {
            if (username == null)
                return false;

            if (username.startsWith("+7")) {
                username = username.replaceAll("\\D", "");
            }
            if (!riasApiService.checkParam(username)) {
                return false;
            }
        } catch (RiasCheckException rce) {
            log.error("RIAS check service", rce);
            return false;
        }

        String location = properties.getProperty(RESET_CREDENTIALS_REDIRECT_URL);
        if (location == null) {
            location = "https://lkb2b.dom.ru/recovery";
        }

        String city = context.getHttpRequest().getDecodedFormParameters().getFirst(FormConstants.FIELD_CITY);
        if (Validation.isBlank(city)) {
            city = "yar";
        }

        location += "?citydomain=" + city;

        log.info("Redirecting to {}", location);

        Response challenge = context.form()
                .setAttribute("redirectTo", location)
                .setAttribute("redirectHeader", username)
                .createForm(RESET_CRED_TO_RIAS_FORM);

        context.challenge(challenge);

        return true;
    }
}
