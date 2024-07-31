package ru.alamics.sso.registration.model;

public class UserConstants {
    private UserConstants () {
    }

    public static final String ACCESS_REALM = "access";
    public static final String DEFAULT_ROLE_ACCESS_REALM = "LPR_pos";

    public static final String ATTR_PHONE_NAME = "phone";
    public static final String ATTR_ORG_NAME = "orgName";
    public static final String ATTR_PHONE_VALIDATED_ON = "phone_validated_on";

    public static final String ATTR_USER_ID_NAME = "userId";
    public static final String ATTR_TOMS_NAME = "tomsId";
    public static final String ATTR_DMP_NAME = "dmpId";
    public static final String ATTR_ROLE_ID_NAME = "roleId";


    public static final String AUTH_FORM_SUCCESS = "AUTH_FORM_SUCCESS";
    public static final String I_FRAME = "iframe";
    public static final String HIDDEN_HEADER = "hiddenHeader";
    public static final String CITY = "city";
    public static final String REDIRECT_URI = "redirect_uri";
    public static final String SELF = "self";

    public static final String DISABLE_TWO_STEP_AUTH = "disable_two_step_auth";

    /**
     * Атрибут пользователя с ID и хэш-суммой отправленного кода
     * (т.к. для direct grant flow сессия на token endpoint не поддерживается)
     */
    public static final String ATTR_REST_SMS_OR_PHONE_CALL_CODE_ID_AND_HASH_KEY
            = "rest_sms_or_phone_call_code_id_and_hash_key";

    /**
     * Атрибут пользователя с меткой времени последней отправки кода в REST авторизации
     */
    public static final String ATTR_REST_SMS_OR_PHONE_CALL_CODE_SENT_AT = "rest_sms_or_phone_call_code_sent_at";

    /**
     * Атрибут пользователя с меткой времени очередной проверки временной блокировки в REST авторизации
     * (кэш на уровне атрибутов пользователя, для того чтобы сократить кол-во обращений в БД)
     */
    public static final String ATTR_REST_SMS_OR_PHONE_CALL_BLOCKED_AT = "attr_rest_sms_or_phone_call_blocked_at";
}
