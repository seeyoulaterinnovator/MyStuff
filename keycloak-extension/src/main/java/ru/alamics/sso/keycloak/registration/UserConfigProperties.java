package ru.alamics.sso.keycloak.registration;

public class UserConfigProperties {
    public static final String HOSTNAME_PROPERTY_NAME = "targetHost";
    public static final String HOSTNAME_PROPERTY_LABEL = "Адрес";
    public static final String HOSTNAME_PROPERTY_HELP_TEXT = "Домен, к которому будут выполняться запросы";
    public static final String PORT_PROPERTY_NAME = "targetPort";
    public static final String PORT_PROPERTY_LABEL = "Порт";
    public static final String PORT_PROPERTY_HELP_TEXT = "Порт, на котором сервер слушает запросы";

    public static final String AUTH_APPNAME_NAME = "authAppname";
    public static final String AUTH_APPNAME_LABEL = "Appname";
    public static final String AUTH_APPNAME_HELP_TEXT = "Параметр appname заголовка авторизации";

    public static final String AUTH_USERNAME_NAME = "authUsername";
    public static final String AUTH_USERNAME_LABEL = "Username";
    public static final String AUTH_USERNAME_HELP_TEXT = "Параметр username заголовка авторизации";

    public static final String PATH_PROPERTY_NAME = "targetPath";
    public static final String PATH_PROPERTY_LABEL = "Путь";
    public static final String PATH_PROPERTY_HELP_TEXT = "Путь, к которому нужно выполнить запрос";

    public static final String SCHEMA_PROPERTY_NAME = "targetSchema";
    public static final String SCHEMA_PROPERTY_LABEL = "HTTPS";
    public static final String SCHEMA_PROPERTY_HELP_TEXT = "Использовать ли шифрованное подключение";
}
