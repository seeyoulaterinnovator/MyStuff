package ru.alamics.sso.registration.tbapi.model;

import lombok.Getter;

@Getter
public enum TbapiConnect {
    REGISTRATION("tbapi.registration.host", "tbapi.registration.port", "tbapi.registration.auth.appname",
            "tbapi.registration.auth.username", "tbapi.registration.auth.password","tbapi.registration.find.path", "tbapi.registration.secure"),
    CUTOMER_NAMES("tbapi.customer.host", "tbapi.customer.port", "tbapi.customer.auth.appname",
            "tbapi.customer.auth.username", "tbapi.customer.auth.password","tbapi.customer.find.path", "tbapi.customer.secure");

    private String host;
    private String port;
    private String appname;
    private String username;
    private String password;
    private String path;
    private String secure;

    TbapiConnect(String host, String port, String appname, String username, String password, String path, String secure) {
        this.host = host;
        this.port = port;
        this.appname = appname;
        this.username = username;
        this.password = password;
        this.path = path;
        this.secure = secure;
    }
}
