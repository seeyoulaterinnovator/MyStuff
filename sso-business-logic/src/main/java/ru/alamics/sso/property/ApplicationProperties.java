package ru.alamics.sso.property;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.jpa.entity.AppProperty;
import ru.alamics.sso.jpa.repository.AppPropertyRepository;
import ru.alamics.sso.util.StandResolver;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.stream.Collectors;

@Singleton
@Startup
@Slf4j
@Lock(LockType.READ)
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

    public int getPropertyInt(final String name) throws PropertyException {

        try {
            return Integer.parseInt(getProperty(name));
        } catch (NumberFormatException nfe) {
            throw new PropertyException(String.format("Can't parse value of '%s'", name), nfe);
        }
    }

    public int getPropertyInt(final String name, int defValue) {

        return getPropertyInt(name, defValue, null);
    }

    public int getPropertyInt(final String name, int defValue, String logDefault) {

        try {
            return Integer.parseInt(getProperty(name));
        } catch (NumberFormatException nfe) {
            if (logDefault != null)
                log.info(logDefault, name, defValue);

            return defValue;
        }
    }

    public long getPropertyLong(final String name) throws PropertyException {

        try {
            return Long.parseLong(getProperty(name));
        } catch (NumberFormatException nfe) {
            throw new PropertyException(String.format("Can't parse value of '%s'", name), nfe);
        }
    }

    public long getPropertyLong(final String name, long defValue) {

        return getPropertyLong(name, defValue, null);
    }

    public long getPropertyLong(final String name, long defValue, String logDefault) {

        try {
            return Long.parseLong(getProperty(name));
        } catch (NumberFormatException nfe) {
            if (logDefault != null)
                log.info(logDefault, name, defValue);

            return defValue;
        }
    }

    @PostConstruct
    @Lock(LockType.WRITE)
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

    @Schedule(hour = "*", minute = "*/10", persistent = false)
    private void initDbProperties() {
        Properties tempProp = new Properties();
        tempProp.putAll(propertyRepository.findAll().stream()
                .collect(Collectors.toMap(AppProperty::getName, AppProperty::getValue)));
        if (!tempProp.isEmpty()) {
            dbProperties = tempProp;
        }
        log.info("Initializing application properties from database finished:{}", dbProperties.toString());
    }

}
