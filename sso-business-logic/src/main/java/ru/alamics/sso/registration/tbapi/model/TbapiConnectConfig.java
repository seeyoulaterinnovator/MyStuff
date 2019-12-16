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

    private static TbapiConnectConfig instance;

    public static TbapiConnectConfig getStaticConfig() {
        if (instance == null) {
            instance = new TbapiConnectConfig();
            instance.setHost("10.121.10.41");
            instance.setPort(26800);
            instance.setAppname("SSP");
            instance.setUsername("anonymous");
            instance.setPath("/api/v1/customerManagement/customerAccount");
            instance.setSecure(false);
        }
        return instance;
    }

    private void setInstance(TbapiConnectConfig instance) {
        //ignore
    }

    private String host;
    private int port;
    private String appname;
    private String username;
    private String path;
    private boolean secure;
}
