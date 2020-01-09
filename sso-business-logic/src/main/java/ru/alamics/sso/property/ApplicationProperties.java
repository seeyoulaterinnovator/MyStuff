package ru.alamics.sso.property;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.keycloak.repository.AppPropertyRepository;
import ru.alamics.sso.util.StandResolver;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Singleton
@Startup
@Slf4j
public class ApplicationProperties {
    private Properties properties;
    @EJB
    private AppPropertyRepository propertyRepository;

    public String getProperty(final String name) {
        String result = System.getenv(name);

        if (result == null) {
            result = System.getProperty(name);
        }
        if (result == null) {
            result = this.properties.getProperty(name);
        }
        if (result == null) {
            result = propertyRepository.findByName(name).getValue();
        }
        return result;
    }

    @PostConstruct
    public void init() throws IOException {
        final String appPropResPath = "/application-" + StandResolver.ENV.name().toLowerCase() + ".properties";
        log.info("Initializing app properties file:\"{}\"", appPropResPath);
        final InputStream stream = ApplicationProperties.class.getResourceAsStream(appPropResPath);
        this.properties = new Properties();
        this.properties.load(stream);
    }
}
