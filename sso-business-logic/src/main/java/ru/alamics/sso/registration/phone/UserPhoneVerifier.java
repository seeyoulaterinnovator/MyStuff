package ru.alamics.sso.registration.phone;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.RealmModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.alamics.sso.registration.model.AuthContext;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.registration.phone.exception.PhoneCallException;
import ru.alamics.sso.registration.phone.exception.SendMessageException;
import ru.alamics.sso.registration.phone.exception.UserPhoneEmpty;
import ru.alamics.sso.registration.phone.exception.WrongSmsCode;
import ru.alamics.sso.registration.phone.model.MessageRequest;
import ru.alamics.sso.registration.phone.model.MessengerType;
import ru.alamics.sso.registration.phone.port.PhoneCallerRemoteService;

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
    private MessageService messageService;
    @EJB
    private PhoneCallerRemoteService phoneCallerService;

    public UserPhoneVerifier() {
    }

    public UserPhoneVerifier(MessageService msgService) {
        this.messageService = msgService;
    }

    public AuthContext sendValidationMsg(User user,
                                         AuthContext context,
                                         ActivationCodeType codeType, RealmModel realm) throws UserPhoneEmpty, PhoneCallException, SendMessageException {
        if (user.getPhone() == null || user.getPhone().isEmpty())
            throw new UserPhoneEmpty();

//        Если в контексте нет хэша - надо отправить смс
        if (context.getHashProperty() == null || !context.getExpirationTime().isAfter(LocalDateTime.now())) {
            log.info("Нет хэша для кода. Повторить получение кода");

            String code = generateCode(user, codeType, context, realm);

            if (code != null) {
                return AuthContext.builder()
                        .expirationTime(context.getExpirationTime())
                        .activationCodeType(codeType)
                        .counter(context.getCounter() + 1)
                        .hashProperty(HashGenerator.getSecretHash(code))
                        .build();
            }
        }
        return context;
    }

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
                messageService.sendMsg(messageRequest);
            }

            return code;
        } else if (ActivationCodeType.CODE_BY_PHONE_NUMBER.equals(codeType)) {
            return phoneCallerService.call(user.getPhone(), context.getCounter());
        }
        return null;
    }

    public void verifyPhone(User user, AuthContext authContext, String smsCode, ActivationCodeType activationCodeType)
            throws WrongSmsCode {
        String savedHash = authContext.getHashProperty();
        LocalDateTime expirationDate = authContext.getExpirationTime();
        //null when send again and enter pass
        String codeHash = HashGenerator.getSecretHash(smsCode);

        if (codeHash.equals(savedHash) && expirationDate.isAfter(LocalDateTime.now())) {
            log.info("Correct sms code");
            if (!ActivationCodeType.CODE_TO_EMAIL.equals(activationCodeType)) {
                user.setPhoneVerifiedOn(LocalDateTime.now());
            }
        } else {
            throw new WrongSmsCode();
        }
    }
}
