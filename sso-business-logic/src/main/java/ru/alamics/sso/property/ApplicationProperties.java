package ru.alamics.sso.property;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


@Singleton
@Startup
public class ApplicationProperties {
    private Properties properties;

    public String getProperty(final String name) {
        String envProperty = System.getenv(name);
        String vmOpts = System.getProperty(name);
        if(envProperty != null) {
            return envProperty;
        } else if(vmOpts != null) {
            return vmOpts;
        } else {
           return this.properties.getProperty(name);
        }
    }

    @PostConstruct
    public void init() throws IOException {
        final InputStream stream = ApplicationProperties.class.getResourceAsStream("/application.properties");
        this.properties = new Properties();
        this.properties.load(stream);
    }
}
