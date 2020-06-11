package ru.alamics.sso.registration.tbapi.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.property.ApplicationProperties;

import javax.naming.InitialContext;

@Slf4j
@Data
public class TbapiConnectConfig {
    private String host;
    private int port;
    private String appname;
    private String username;
    private String password;
    private String path;
    private boolean secure;

    public TbapiConnectConfig() {}
    
    public TbapiConnectConfig(TbapiConnect connect) {
        try {
            ApplicationProperties properties = (ApplicationProperties) new InitialContext().lookup("java:global/domru-sso/" + ApplicationProperties.class.getSimpleName());
            host = properties.getProperty(connect.getHost());
            port = properties.getPropertyInt(connect.getPort());
            appname = properties.getProperty(connect.getAppname());
            username = properties.getProperty(connect.getUsername());
            password = properties.getProperty(connect.getPassword());
            secure = Boolean.parseBoolean(properties.getProperty(connect.getSecure()));
            path = properties.getProperty(connect.getPath());
        } catch (Exception e) {
            log.error("Error read tbapi configuration");
        }
    }
}
