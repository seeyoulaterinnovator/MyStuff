package ru.alamics.sso.property;

import ru.alamics.sso.keycloak.entity.Settings;
import ru.alamics.sso.keycloak.repository.SettingsRepository;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


@Singleton
@Startup
public class ApplicationProperties {
    private Properties properties;

    @EJB
    private SettingsRepository repository;


    public String getProperty(final PropertyConstants property, final String realmId) {
        final String keyName = property.getKey();
        Settings settings = repository.getSettings(keyName, realmId);
        String ret;
        if(settings != null) {
            ret = settings.getValue();
        } else {
            String envProperty = System.getenv(keyName);
            String vmOpts = System.getProperty(keyName);
            if(envProperty != null) {
                ret = envProperty;
            } else if(vmOpts != null) {
                ret = vmOpts;
            } else {
                ret = this.properties.getProperty(keyName);
            }
        }

        return ret;
    }

    @PostConstruct
    public void init() throws IOException {
        final InputStream stream = ApplicationProperties.class.getResourceAsStream("/application.properties");
        this.properties = new Properties();
        this.properties.load(stream);
    }
}
