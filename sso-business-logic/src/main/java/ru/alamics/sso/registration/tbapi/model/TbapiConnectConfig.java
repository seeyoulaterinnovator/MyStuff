package ru.alamics.sso.registration.tbapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TbapiConnectConfig {

    private String host;
    private int port;
    private String appname;
    private String username;
    private String path;
    private boolean secure;
}
