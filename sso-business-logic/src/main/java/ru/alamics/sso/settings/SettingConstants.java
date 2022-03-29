package ru.alamics.sso.settings;

/*
    Большая часть текста была перенесена из
    файла messages_ru.properties далее эти поля можно будет менять через
    Админ консоль
 */
public enum SettingConstants {
    ABSENCE_BLOCKING_DAYS("user.absence.blocking.days"),
    ABSENCE_NOTIFICATION_DAYS("user.absence.notifications.days"),
    EXPIRE_SMS_VIBER_CODE("user.expire.sms-viber.code"),
    EXPIRE_INCOMING_CALL_CODE("user.expire.incoming.call.code"),
    EXPIRE_INCOMING_CALL_EMAIL_CODE("user.expire.incoming.call.email.code"),


    //параметры шлюзов сообщений СМС и Viber
    SEND_URI_SMS("smsSender.uri"),
    SMSC_NAME_SMS("smsSender.smscName"),
    USERNAME_SMS("smsSender.username"),
    PASSWORD_SMS("smsSender.password"),
    SENDER_NAME_SMS("smsSender.senderName"),
    TIMEOUT_SMS("smsSender.timeout"),

    SEND_URI_VIBER("viberSender.uri"),
    SMSC_NAME_VIBER("viberSender.smscName"),
    USERNAME_VIBER("viberSender.username"),
    PASSWORD_VIBER("viberSender.password"),
    SENDER_NAME_VIBER("viberSender.senderName"),
    TIMEOUT_VIBER("viberSender.timeout"),


    //Составные параметры переменных для шлюза
    SEND_URI(".uri"),
    SMSC_NAME(".smscName"),
    USERNAME_SENDER(".username"),
    PASSWORD(".password"),
    SENDER_NAME(".senderName"),
    TIMEOUT(".timeout"),


    //Переменные страниц

    ENTER("enter"),
    PASS_PLACEHOLDER("passwordPlaceholder"),
    DO_FORGOT_PASS("doForgotPassword"),
    REGISTER_TITLE("registerTitle"),
    LOGIN_TITLE_TEXT("loginTitleText"),
    YOUR_LOGIN("yourlogin"),
    PASS("password"),
    LOGIN_WITH("loginWith"),
    DO_LOGIN("doLogIn"),
    DO_ACCEPT("doAccept"),
    FOOTER("footer"),
    USERNAME_OR_EMAIL_PLACEHOLDER("usernameOrEmailPlaceholder"),
    CHOOSE_ON_ORGANIZATION("chooseOrganization"),
    ORGANIZATION("Organization"),
    ROLE_USER("roleUser"),
    BACK_TO_APP("backToApplication"),
    ACCOUNT_UPDATE_MESSAGE("accountUpdatedMessage"),
    CONFIRM_LINK_IDP_REVIEW_PROFILE("confirmLinkIdpReviewProfile"),
    CONFIRM_LINK_IDP_CONTINUE("confirmLinkIdpContinue"),
    PROCEED_WITH_ACTION("proceedWithAction"),
    PAGE_EXPIRE_MSG_1("pageExpiredMsg1"),
    PAGE_EXPIRE_MSG_2("pageExpiredMsg2"),
    DO_CLICK_HERE("doClickHere"),
    EMAIL_FORGOT_CONTENT_TITLE("emailForgotContentTitle"),
    DO_CANCEL("doCancel"),
    USERNAME("username"),
    PHONE_OR_EMAIL("phoneOrEmail"),
    NEXT("next"),
    EMAIL_INSTRUCTION("emailInstruction"),
    USERNAME_OR_EMAIL("usernameOrEmail"),
    RESET_PASSWORD("resetPassword"),
    LOGIN_PROFILE_TITLE("loginProfileTitle"),
    DO_REGISTER("doRegister"),
    DO_SUBMIT("doSubmit"),
    LOGIN_TO_B2B("loginToB2B"),
    BACK_TO_MAIN_PAGE("backToMainPage"),
    SEND_AGAIN("sendAgain"),
    SEND_BY_EMAIL("sendByEmail"),
    PHONE_CONST("phoneConst"),
    REQUIRED_FIELDS("requiredFields"),
    PLACEHOLDER_USERNAME("placeholderUsername"),
    PLACEHOLDER_EMAIL("placeholderEmail"),
    PLACEHOLDER_PHONE("placeholderPhone"),
    PHONE_CONST_LINK("phoneConstLink"),

    //Заголовоки сообщений

    ACCOUNT_SUBJECT("emailAccountDataSubject"),
    ACCOUNT_SUBJECT_ENABLE("emailEnabledAccountSubject"),
    ACCOUNT_SUBJECT_DISABLE("emailDisabledAccountSubject"),
    ACCOUNT_SUBJECT_SEND_LOGIN("emailSendLoginSubject"),
    ACCOUNT_SUBJECT_RESET_PASSWORD("emailResetPasswordSubject"),
    ACCOUNT_SUBJECT_VERIFICATION_AUTH("emailVerificationAuthSubject"),
    ACCOUNT_SUBJECT_CREDENTIAL_DISABLE("emailCredentialDisableSubject"),
    ACCOUNT_SUBJECT_VERIFICATION("emailVerificationSubject"),
    ACCOUNT_SUBJECT_ACTIONS("executeActionsSubject"),
    ACCOUNT_SUBJECT_PROVIDER_LINK("identityProviderLinkSubject"),
    ACCOUNT_SUBJECT_RESET("passwordResetSubject"),

    //Переменные сообщений

    PHONE_IN_MESSAGE("phoneInMessage"),
    FOOTER_IN_MESSAGE("footerInMassage"),
    CUSTOMER("customer"),
    GRATITUDE_UP("GratitudeUp"),
    GRATITUDE_DOWN("GratitudeDown"),

    //body сообщений

    EMAIL_ENABLE_ACCOUNT("emailEnabledAccountBodyHtml"),
    EMAIL_DISABLE_ACCOUNT("emailDisabledAccountBodyHtml"),
    EMAIL_CREATE_ACCOUNT("emailAccountCreateBodyHtml"),
    EMAIL_SEND_LOGIN_ACCOUNT("emailSendLoginBodyHtml"),
    EMAIL_LOGIN_AND_PHONE_ACCOUNT("emailLoginAndPhoneHtml"),
    EMAIL_LOGIN_ACCOUNT("emailLoginHtml"),
    EMAIL_PASSWORD_FOOTER_ACCOUNT("emailPasswordFooterHtml"),
    EMAIL_RESET_PASSWORD_ACCOUNT("emailResetPasswordBodyHtml"),
    EMAIL_VERIFICATION_AUTH_ACCOUNT("emailVerificationAuthBodyHtml"),
    EMAIL_CREDENTIAL_DISABLE_ACCOUNT("emailCredentialDisableBodyHtml"),
    EMAIL_ACTIONS_ACCOUNT("executeActionsBodyHtml"),
    EMAIL_VERIFICATION_LOGIN_ACCOUNT("emailVerificationLoginBodyHtml"),
    EMAIL_VERIFICATION_ACCOUNT("emailVerificationBodyHtml"),
    EMAIL_IDENTITY_PROVIDER("identityProviderLinkBodyHtml"),
    EMAIL_RESET("passwordResetBodyHtml"),
    EMAIL_DATE_ACCOUNT("emailAccountDataBodyHtml"),
    EMAIL_LINK_PASSWORD("linkPassword"),

    SCHEDULER_BLOCKING_BODY("blockNotificationSchedulerHtml"),
    SCHEDULER_BLOCKING_PREPARE_BODY("blockPrepareNotificationSchedulerHtml"),
    SCHEDULER_PASSWORD_EXPIRES_BODY("passwordExpiresSchedulerHtml"),

    HOME_PAGE("homePageSystem"),
    TIMER_INTERVAL_DURATION_PROPERTY("timerIntervalDurationProperty"),
    DEFAULT_REALM_CLIENT_ID("defaultRealmClient"),

    TIME_TOKEN_VERIFY_EMAIL("life.token.loginverify.email"),
    TIME_TOKEN_RESET_PASSWORD("life.token.reset.pass"),
    TIME_TOKEN_RESET_PASSWORD_AND_LOGIN("life.token.reset.pass.login"),
    TIME_TOKEN_SET_FIRST_PASS("life.token.set.first-pass"),

    TOKEN_DADATA("tokenDaData"),
    URL_DADATA_REQUEST_LOCATION_IP("urlDaDataRequestLocationIp"),

    BLOCK_NOTIFICATION_OF_WARNING("block.notification.warning"),
    BLOCK_NOTIFICATION_OF_BLOCKED("block.notification.blocked"),
    BLOCK_NOTIFICATION_OF_UNLOCKING("block.notification.unlocking");

    private final String key;

    SettingConstants(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
