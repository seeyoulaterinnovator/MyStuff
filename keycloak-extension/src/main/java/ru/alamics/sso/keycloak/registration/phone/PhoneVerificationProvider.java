package ru.alamics.sso.keycloak.registration.phone;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.alamics.sso.keycloak.registration.mapper.UserModelUserMapper;
import ru.alamics.sso.registration.model.AuthContext;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.registration.phone.UserPhoneVerifier;
import ru.alamics.sso.registration.phone.exception.UserPhoneAlreadyVerified;
import ru.alamics.sso.registration.phone.exception.UserPhoneEmpty;
import ru.alamics.sso.registration.phone.exception.WrongSmsCode;

import javax.ws.rs.core.Response;

@Slf4j
public class PhoneVerificationProvider implements RequiredActionProvider {

    private static final String PHONE_KEY_HASH = "phone_key_hash";
    private static final String VERIFY_PHONE_FTL = "verifyPhone.ftl";
    private final UserModelUserMapper mapper;
    private final UserPhoneVerifier userPhoneVerifier;

    public PhoneVerificationProvider(UserModelUserMapper mapper, UserPhoneVerifier userPhoneVerifier) {
        this.mapper = mapper;
        this.userPhoneVerifier = userPhoneVerifier;
    }

    @Override
    public void evaluateTriggers(RequiredActionContext context) {

    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        log.info("PhoneRequiredActionChallenge");

        AuthenticationSessionModel authSession = context.getAuthenticationSession();

        User user = mapper.mapToUser(context.getUser());

        AuthContext authContext = AuthContext.builder()
                .property(PHONE_KEY_HASH, authSession.getAuthNote(PHONE_KEY_HASH))
                .build();

        try {
            authContext = userPhoneVerifier.sendValidationSms(user, authContext);

            authSession.removeAuthNote(PHONE_KEY_HASH);
            authSession.setAuthNote(PHONE_KEY_HASH, authContext.getProperties().get(PHONE_KEY_HASH));

            Response challenge = context.form().createForm(VERIFY_PHONE_FTL);
            context.challenge(challenge);
        } catch (UserPhoneEmpty userPhoneEmpty) {
            context.ignore();
        } catch (UserPhoneAlreadyVerified userPhoneAlreadyVerified) {
            context.success();
        }
    }

    @Override
    public void processAction(RequiredActionContext context) {
        log.info("PhoneProcessAction");

        AuthenticationSessionModel authSession = context.getAuthenticationSession();

        if (context.getHttpRequest().getDecodedFormParameters().containsKey("resend")) {
            log.info("Sms code resend");

            authSession.removeAuthNote(PHONE_KEY_HASH);

            requiredActionChallenge(context);

        } else {
            UserModel model = context.getUser();
            User user = mapper.mapToUser(model);

            AuthContext authContext = AuthContext.builder()
                    .property(PHONE_KEY_HASH, authSession.getAuthNote(PHONE_KEY_HASH))
                    .build();

            try {
                String code = context.getHttpRequest().getDecodedFormParameters().getFirst("smscode");

                userPhoneVerifier.verifyPhone(user, authContext, code);

                mapper.mergeUserInto(user, model);
                authSession.removeAuthNote(PHONE_KEY_HASH);
                context.success();
            } catch (WrongSmsCode wrongSmsCode) {
                log.warn("Wrong sms code");
                Response challenge = context.form()
                        .setError("Введен некорректный код смс")
                        .createForm(VERIFY_PHONE_FTL);
                context.challenge(challenge);
            }
        }
    }

    @Override
    public void close() {

    }
}
