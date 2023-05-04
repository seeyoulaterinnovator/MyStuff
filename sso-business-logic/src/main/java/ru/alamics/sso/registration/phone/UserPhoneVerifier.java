package ru.alamics.sso.registration.phone;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.RealmModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.alamics.sso.registration.model.AuthContext;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.registration.phone.exception.*;
import ru.alamics.sso.registration.phone.model.MessageRequest;
import ru.alamics.sso.registration.phone.model.MessengerType;
import ru.alamics.sso.registration.phone.port.PhoneCallerRemoteService;
import ru.alamics.sso.registration.phone.port.SendMessageService;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Stateless
public class UserPhoneVerifier {

    public static final String PHONE_KEY_HASH = "phone_key_hash";
    public static final String EXPIRATION_TIME = "expiration_time";
    public static final String COUNT_REPEAT = "count_repeat";
    public static final String MESSENGER = "messenger";

    @EJB
    private SendMessageService messageSendService;
    @EJB
    private PhoneCallerRemoteService phoneCallerService;

    public UserPhoneVerifier() {
        System.out.println("Got SendMessageService");
        System.out.println("Got PhoneCallerRemoteService");
    }

    // TODO: удалить
    public AuthContext sendValidationMsg(User user,
                                         AuthContext authContext,
                                         ActivationCodeType codeType, RealmModel realm) throws UserPhoneEmpty, PhoneCallException, SendMessageException {
        if (user.getPhone() == null || user.getPhone().isEmpty())
            throw new UserPhoneEmpty();

//        Если в контексте нет хэша - надо отправить смс
        if (authContext.getHashProperty() == null || !authContext.getExpirationTime().isAfter(LocalDateTime.now())) {
            log.info("Нет хэша для кода. Повторить получение кода");

            String code = generateCode(user, codeType, authContext, realm);

            if (code != null) {
                return AuthContext.builder()
                        .expirationTime(authContext.getExpirationTime()) // useless
                        .activationCodeType(codeType) // useless
                        .counter(authContext.getCounter() + 1) // useless
                        .hashProperty(HashGenerator.getSecretHash(code)) // useless
                        .build();
            }
        }
        return authContext;
    }

    // TODO: удалить
    private String generateCode(User user, ActivationCodeType codeType, AuthContext context, RealmModel realm)
            throws PhoneCallException, SendMessageException {
        if (ActivationCodeType.CODE_TO_SMS.equals(codeType)) {
            String code = SmsCodeGenerator.getCode(codeType.getLengthCode());

            MessageRequest messageRequest = MessageRequest.builder()
                    .userPhone(user.getPhone())
                    .text(code)
                    .realmId(realm.getId())
                    .build();

            String[] listMessenger = realm.getSmtpConfig().get(MESSENGER).split(",");

            for (String messenger: listMessenger) {
                messageRequest.setMessengerName(MessengerType.valueOf(messenger));
                messageSendService.sendMessageByRequestAndLogInfo(messageRequest);
            }

            return code;
        } else if (ActivationCodeType.CODE_BY_PHONE_NUMBER.equals(codeType)) {
            return phoneCallerService.callAndGetCode(user.getPhone(), context.getCounter());
        }
        return null;
    }

    public void verifyPhone(User user, LocalDateTime expirationTime, String savedCodeHash, String smsCode, ActivationCodeType activationCodeType) throws WrongSmsCode, TimeExpiredException {
        String codeHash = HashGenerator.getSecretHash(smsCode);

        if (!codeHash.equals(savedCodeHash)) {
            throw new WrongSmsCode();
        }

        if (expirationTime.isBefore(LocalDateTime.now())) {
            throw new TimeExpiredException();
        }

        // TODO: WTF??
        if (!ActivationCodeType.CODE_TO_EMAIL.equals(activationCodeType)) {
            user.setPhoneVerifiedOn(LocalDateTime.now());
        }
    }
}
