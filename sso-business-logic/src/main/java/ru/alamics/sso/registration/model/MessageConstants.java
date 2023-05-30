package ru.alamics.sso.registration.model;

public class MessageConstants {

    public static final String PHONE_EXISTS = "Номер мобильного телефона уже используется в другой учетной записи. Если Вы уже регистрировались, попробуйте войти в свою учетную запись, либо укажите другой номер телефона.";
    public static final String PHONE = "телефоном";
    public static final String SMS_LIMIT_BLOCK = "Превышен лимит СМС. Запросить новое СМС можно через 12 часов";
    public static final String SMS_LIMIT_5_CONTINUE = "Код был введён более 5 раз. Запросите новое СМС";
    public static final String CALL_LIMIT_BLOCK = "Превышен лимит повторных звонков. Запросить новый звонок можно через 12 часов";
    public static final String CALL_LIMIT_5_CONTINUE = "Код был введён более 5 раз. Запросите новый звонок";

    public static final String PHONE_INVALID = "Невалидный номер телефона";
}
