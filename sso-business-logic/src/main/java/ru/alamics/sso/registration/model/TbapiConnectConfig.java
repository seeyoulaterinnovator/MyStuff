package ru.alamics.sso.registration.model;

import lombok.Data;

@Data
public class TbapiConnectConfig {

    private String host;
    private int port;
    private String appname;
    private String username;
    private String path;
    private boolean secure;
}
