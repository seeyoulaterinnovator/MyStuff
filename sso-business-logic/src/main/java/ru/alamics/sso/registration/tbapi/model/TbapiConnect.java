package ru.alamics.sso.registration.tbapi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TbapiConnect {
    REGISTRATION("tbapi.registration.host", "tbapi.registration.ip", "tbapi.registration.port", "tbapi.registration.auth.appname",
            "tbapi.registration.auth.username", "tbapi.registration.auth.password","tbapi.registration.find.path", "tbapi.registration.secure"),
    CUTOMER_NAMES("tbapi.customer.host", "tbapi.registration.ip","tbapi.customer.port", "tbapi.customer.auth.appname",
            "tbapi.customer.auth.username", "tbapi.customer.auth.password","tbapi.customer.find.path", "tbapi.customer.secure");

    private String host;
    private String ip;
    private String port;
    private String appname;
    private String username;
    private String password;
    private String path;
    private String secure;

}
