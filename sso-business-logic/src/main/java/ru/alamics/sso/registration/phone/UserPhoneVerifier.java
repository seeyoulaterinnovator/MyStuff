package ru.alamics.sso.registration.phone;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.registration.model.AuthContext;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.registration.phone.exception.UserPhoneAlreadyVerified;
import ru.alamics.sso.registration.phone.exception.UserPhoneEmpty;
import ru.alamics.sso.registration.phone.exception.WrongSmsCode;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.time.LocalDateTime;

@Slf4j
@Stateless
public class UserPhoneVerifier {

    private static final String PHONE_KEY_HASH = "phone_key_hash";

    @EJB
    private SmsCodeGenerator smsCodeGenerator;
    @EJB
    private SmsService smsService;
    @EJB
    private HashProvider hashProvider;

    public UserPhoneVerifier() {

    }

    public UserPhoneVerifier(SmsCodeGenerator smsCodeGenerator, SmsService smsService, HashProvider hashProvider) {
        this.smsCodeGenerator = smsCodeGenerator;
        this.smsService = smsService;
        this.hashProvider = hashProvider;
    }

    public AuthContext sendValidationSms(User user, AuthContext context) throws UserPhoneEmpty, UserPhoneAlreadyVerified {
        if (user.getPhone() == null || user.getPhone().isBlank())
            throw new UserPhoneEmpty();
        if (user.getPhoneVerifiedOn() != null)
            throw new UserPhoneAlreadyVerified();

//        Если в контексте нет хэша - надо отправить смс
        if (context.getProperties().get(PHONE_KEY_HASH) == null) {
            String code = smsCodeGenerator.getCode();

            smsService.sendSms(user.getId(), user.getPhone(), code);

            String hash = hashProvider.getHash(code);

            AuthContext.AuthContextBuilder builder = AuthContext.builder();

            context.getProperties().forEach(builder::property);
            builder.property(PHONE_KEY_HASH, hash);

            return builder.build();
        }
        return context;
    }

    public void verifyPhone(User user, AuthContext authContext, String smsCode) throws WrongSmsCode {

        String savedHash = authContext.getProperties().get(PHONE_KEY_HASH);

        String codeHash = hashProvider.getHash(smsCode);

        if (codeHash.equals(savedHash)) {
            log.info("Correct sms code");
            user.setPhoneVerifiedOn(LocalDateTime.now());
        } else {
            throw new WrongSmsCode();
        }

    }
}
