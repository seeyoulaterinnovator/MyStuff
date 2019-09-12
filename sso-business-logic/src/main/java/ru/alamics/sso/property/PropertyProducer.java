package ru.alamics.sso.property;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class PropertyProducer {
    private Properties properties;

    @Property
    @Produces
    public String produceString(final InjectionPoint ip) {
        String key = getKey(ip);
        log.info("key={}", key);
        return this.properties.getProperty(key);
    }

    @Property
    @Produces
    public Integer produceInt(final InjectionPoint ip) {
        String key = getKey(ip);
        return Integer.parseInt(this.properties.getProperty(key));
    }

    @Property
    @Produces
    public boolean produceBoolean(final InjectionPoint ip) {
        return Boolean.parseBoolean(this.properties.getProperty(getKey(ip)));
    }

    private String getKey(final InjectionPoint ip) {
        return (ip.getAnnotated().isAnnotationPresent(Property.class) &&
                !ip.getAnnotated().getAnnotation(Property.class).value().isEmpty()) ? ip.getAnnotated()
                .getAnnotation(Property.class).value() : ip.getMember().getName();
    }

    @PostConstruct
    public void init() {
        log.info("---------------------------------------------------------------------------------------------------------------------");
        this.properties = new Properties();
        final InputStream stream = PropertyProducer.class
                .getResourceAsStream("/application.properties");
        if (stream == null) {
            throw new RuntimeException("No properties!!!");
        }
        try {
            this.properties.load(stream);
        } catch (final IOException e) {
            throw new RuntimeException("Configuration could not be loaded!");
        }
    }
}
