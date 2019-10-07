package ru.alamics.sso.property;

import ru.alamics.sso.keycloak.entity.Settings;
import ru.alamics.sso.keycloak.repository.SettingsRepository;
import ru.alamics.sso.registration.mapper.DataMapper;
import ru.alamics.sso.settings.SettingsDto;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;


@Singleton
@Startup
public class ApplicationProperties {
    private Properties properties;

    @EJB
    private SettingsRepository repository;


    public String getProperty(final PropertyConstants property, final String realmId, final boolean isConvertToSeconds) {
        final String keyName = property.getKey();
        Settings settings = repository.getSettings(keyName, realmId);
        String ret;
        if(settings != null) {
            ret = settings.getValue();
            if (isConvertToSeconds) {
                ret = String.valueOf(TimeUnit.SECONDS.convert(Long.parseLong(ret), settings.getUnit()));
            }
        } else {
            ret = getProperty(keyName);
        }

        return ret;
    }

    public String getProperty(final String name) {
        String envProperty = System.getenv(name);
        String vmOpts = System.getProperty(name);
        String ret;
        if(envProperty != null) {
            ret = envProperty;
        } else if(vmOpts != null) {
            ret = vmOpts;
        } else {
            ret = this.properties.getProperty(name);
        }
        return ret;
    }

    public SettingsDto getSetting(final PropertyConstants property, final String realmId){
        final String keyName = property.getKey();
        return DataMapper.toDto(repository.getSettings(keyName, realmId));
    }

    @PostConstruct
    public void init() throws IOException {
        final InputStream stream = ApplicationProperties.class.getResourceAsStream("/application.properties");
        this.properties = new Properties();
        this.properties.load(stream);
    }
}
