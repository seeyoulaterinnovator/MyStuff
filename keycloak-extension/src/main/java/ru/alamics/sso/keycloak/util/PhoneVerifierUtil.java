package ru.alamics.sso.keycloak.util;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.RequiredActionContext;
import ru.alamics.sso.keycloak.auth.form.new_auth.PhonePlusRealmProtector;
import ru.alamics.sso.registration.phone.ActivationCodeType;
import ru.alamics.sso.settings.SettingsService;

import java.util.Map;

import static ru.alamics.sso.registration.phone.ActivationCodeType.CODE_BY_PHONE_NUMBER;
import static ru.alamics.sso.registration.phone.ActivationCodeType.CODE_TO_SMS;
import static ru.alamics.sso.settings.SettingConstants.EXPIRE_INCOMING_CALL_CODE;
import static ru.alamics.sso.settings.SettingConstants.EXPIRE_SMS_VIBER_CODE;

public class PhoneVerifierUtil {
    /*private static final SettingsService settingsService = new SettingsService();*/
    public static long getCodeLifeTime(ActivationCodeType type, AuthenticationFlowContext context) {
        SettingsService settingsService = new SettingsService();
        long codeLifeTime = 0;
        if (type.equals(CODE_TO_SMS)) {
            codeLifeTime = settingsService.getSettingsLongValue(EXPIRE_SMS_VIBER_CODE, context.getRealm().getName()) * 60; //we need seconds
            return codeLifeTime;
        }
        if (type.equals(CODE_BY_PHONE_NUMBER)) {
            codeLifeTime = settingsService.getSettingsLongValue(EXPIRE_INCOMING_CALL_CODE, context.getRealm().getName()) * 60; //we need seconds
            return codeLifeTime;
        }
        return codeLifeTime;
    }

    public static long getCodeLifeTime(ActivationCodeType type, RequiredActionContext context) {
        SettingsService settingsService = new SettingsService();
        long codeLifeTime = 0;
        if (type.equals(CODE_TO_SMS)) {
            codeLifeTime = settingsService.getSettingsLongValue(EXPIRE_SMS_VIBER_CODE, context.getRealm().getName());
            return codeLifeTime;
        }
        if (type.equals(CODE_BY_PHONE_NUMBER)) {
            codeLifeTime = settingsService.getSettingsLongValue(EXPIRE_INCOMING_CALL_CODE, context.getRealm().getName());
            return codeLifeTime;
        }
        return codeLifeTime;
    }

    public static boolean countLastAttemptIsMoreThan5(Map<PhonePlusRealmProtector, Integer> map, PhonePlusRealmProtector protector) {
        if (map.get(protector) == null ) {
            map.put(protector, 0);
        }
        else {
            map.put(protector, map.get(protector) + 1);
        }
        return map.get(protector) >= 5;
    }
}
