package ru.alamics.sso.property;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.keycloak.entity.AppProperty;
import ru.alamics.sso.keycloak.repository.AppPropertyRepository;
import ru.alamics.sso.util.StandResolver;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Properties;
import java.util.stream.Collectors;

@Singleton
@Startup
@Slf4j
public class ApplicationProperties {
    private Properties fileProperties = new Properties();
    private Properties dbProperties = new Properties();
    @EJB
    private AppPropertyRepository propertyRepository;

    public String getProperty(final String name) {
        String result = System.getenv(name);

        if (result == null) {
            result = System.getProperty(name);
        }
        if (result == null) {
            result = this.fileProperties.getProperty(name);
        }
        if (result == null) {
            result = this.dbProperties.getProperty(name);
        }
        return result;
    }

    @PostConstruct
    public void init() throws IOException {
        initDbProperties();
        initFileProperties();
    }

    private void initFileProperties() throws IOException {
        final String appPropResPath = "/" + StandResolver.ENV_CONFIG + "/application.properties";
        log.info("Initializing app properties file:\"{}\"", appPropResPath);
        final InputStream stream = ApplicationProperties.class.getResourceAsStream(appPropResPath);
        this.fileProperties.load(stream);
        log.info("Initializing application properties from file finished:{}", fileProperties.toString());
    }

    @Schedule(hour = "*/1", persistent = false)
    private void initDbProperties() {
        dbProperties.putAll(propertyRepository.findAll().stream()
                .collect(Collectors.toMap(AppProperty::getName, AppProperty::getValue)));
        log.info("Initializing application properties from database finished:{}", dbProperties.toString());
    }
}
