package ru.alamics.sso.property;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.keycloak.entity.AppProperty;
import ru.alamics.sso.keycloak.repository.AppPropertyRepository;
import ru.alamics.sso.util.StandResolver;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.stream.Collectors;

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
        return result;
    }

    @PostConstruct
    @Schedule(hour = "*/1", persistent = false)
    public void init() throws IOException {
        this.properties = new Properties();
        initPropertiesFromDb();
        initPropertiesFromFile();
        log.info("Initializing application properties finished:{}", properties.toString());
    }

    private void initPropertiesFromFile() throws IOException {
        final String appPropResPath = "/" + StandResolver.ENV_CONFIG + "/application.properties";
        log.info("Initializing app properties file:\"{}\"", appPropResPath);
        final InputStream stream = ApplicationProperties.class.getResourceAsStream(appPropResPath);
        this.properties.load(stream);
    }

    private void initPropertiesFromDb() {
        properties.putAll(propertyRepository.findAll().stream()
                .collect(Collectors.toMap(AppProperty::getName, AppProperty::getValue)));
    }
}
