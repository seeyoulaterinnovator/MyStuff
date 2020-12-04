package ru.alamics.sso.remote;

import ru.alamics.sso.property.ApplicationProperties;

import java.util.Properties;

public class ApplicationPropertiesMock extends ApplicationProperties {

    private Properties properties;

    public ApplicationPropertiesMock(Properties properties) {

        this.properties = properties;
    }

    @Override
    public String getProperty(final String name) {
        String result = System.getenv(name);

        if (result == null) {
            result = System.getProperty(name);
        }

        if (result == null) {
            result = properties.getProperty(name);
        }

        return result;
    }
}
