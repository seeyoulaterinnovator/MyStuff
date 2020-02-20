package ru.alamics.sso.registration.phone;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.registration.model.AuthContext;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.registration.phone.exception.*;
import ru.alamics.sso.registration.phone.port.PhoneCallerRemoteService;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.time.LocalDateTime;

@Slf4j
@Stateless
public class UserPhoneVerifier {

    public static final String PHONE_KEY_HASH = "phone_key_hash";
    public static final String EXPIRATION_TIME = "expiration_time";
    public static final String COUNT_REPEAT = "count_repeat";

    @EJB
    private SmsService smsService;
    @EJB
    private ViberService viberService;
    @EJB
    private PhoneCallerRemoteService phoneCallerService;

    public UserPhoneVerifier() {
    }

    public UserPhoneVerifier(SmsService smsService) {
        this.smsService = smsService;
    }

    public AuthContext sendValidationSms(User user,
                                         AuthContext context,
                                         ActivationCodeType codeType) throws UserPhoneEmpty, PhoneCallException, SmsSendException {
        if (user.getPhone() == null || user.getPhone().isEmpty())
            throw new UserPhoneEmpty();

//        Если в контексте нет хэша - надо отправить смс
        if (context.getHashProperty() == null || !context.getExpirationTime().isAfter(LocalDateTime.now())) {
            log.info("Нет хэша для кода. Повторить получение кода");

            String code = generateCode(user, codeType, context);

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

    private String generateCode(User user, ActivationCodeType codeType, AuthContext context)
            throws PhoneCallException, SmsSendException
    {
        if ( ActivationCodeType.CODE_TO_SMS.equals(codeType)) {
            String code = SmsCodeGenerator.getCode(codeType.getLengthCode());

            try {
                viberService.sendMsg(user.getId(), user.getPhone(), code);
            } catch (ViberSendException e) {

                smsService.sendSms(user.getId(), user.getPhone(), code);
            }

            return code;
        } else if (ActivationCodeType.CODE_BY_PHONE_NUMBER.equals(codeType)) {
            return phoneCallerService.call(user.getPhone(), context.getCounter());
        }
        return null;
    }

    public void verifyPhone(User user, AuthContext authContext, String smsCode, ActivationCodeType activationCodeType)
            throws WrongSmsCode
    {
        String savedHash = authContext.getHashProperty();
        LocalDateTime expirationDate = authContext.getExpirationTime();

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
