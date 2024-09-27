package ru.alamics.sso.property;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.Locked;
import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.jpa.entity.AppProperty;
import ru.alamics.sso.jpa.repository.AppPropertyRepository;
import ru.alamics.sso.util.E2EUtil;
import ru.alamics.sso.util.StandResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class ApplicationProperties {
    private final Properties fileProperties = new Properties();

    private final AtomicReference<Properties> dbPropertiesRef = new AtomicReference<>(new Properties());

    @Inject
    AppPropertyRepository propertyRepository;

    private ScheduledExecutorService executor;

    void onStart(@Observes StartupEvent ev) {
        executor = Executors.newSingleThreadScheduledExecutor();
        int defaultDelay = 60 * 10;
        int delay = defaultDelay;
        try {
            delay = Integer.parseInt(System.getenv("APP_PROPS_UPDATE_DELAY_SECS"));
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
        if(delay < 0) {
            delay = defaultDelay;
        }
        executor.scheduleWithFixedDelay(this::initDbProperties, delay, delay, TimeUnit.SECONDS);
    }

    void onShutdown(@Observes ShutdownEvent ev) {
        if(executor != null) {
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.warn(e.getMessage(), e);
                executor.shutdownNow();
            }
        }
    }

    @Locked.Read
    public String getProperty(final String name) {
        String result = System.getenv(name);
        if (result == null) {
            result = System.getProperty(name);
        }
        if (result == null) {
            if (E2EUtil.isE2E()) {
                try {
                    result = this.propertyRepository.findByName(name).map(AppProperty::getValue).orElse(null);
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            } else {
                result = this.dbPropertiesRef.get().getProperty(name);
            }
        }
        if (result == null) {
            result = this.fileProperties.getProperty(name);
        }
        return result;
    }

    @Locked.Read
    public int getPropertyInt(final String name) throws PropertyException {
        try {
            return Integer.parseInt(getProperty(name));
        } catch (NumberFormatException nfe) {
            throw new PropertyException(String.format("Can't parse value of '%s'", name), nfe);
        }
    }

    @Locked.Read
    public int getPropertyInt(final String name, int defValue) {
        return getPropertyInt(name, defValue, null);
    }

    @Locked.Read
    public int getPropertyInt(final String name, int defValue, String logDefault) {
        try {
            return Integer.parseInt(getProperty(name));
        } catch (NumberFormatException nfe) {
            if (logDefault != null)
                log.info(logDefault, name, defValue);

            return defValue;
        }
    }

    @Locked.Read
    public long getPropertyLong(final String name) throws PropertyException {
        try {
            return Long.parseLong(getProperty(name));
        } catch (NumberFormatException nfe) {
            throw new PropertyException(String.format("Can't parse value of '%s'", name), nfe);
        }
    }

    @Locked.Read
    public long getPropertyLong(final String name, long defValue) {
        return getPropertyLong(name, defValue, null);
    }

    @Locked.Read
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
    @Locked.Write
    public void init() throws IOException {
        initFileProperties();
        try {
            initDbProperties();
        } catch (Exception e) {
            if(E2EUtil.isE2E()) {
                log.error(e.getMessage());
            } else {
                throw e;
            }
        }
    }

    private void initFileProperties() throws IOException {
        final String appPropResPath = "/" + StandResolver.ENV_CONFIG + "/application.properties";
        log.info("Initializing app properties file:\"{}\"", appPropResPath);
        final InputStream stream = ApplicationProperties.class.getResourceAsStream(appPropResPath);
        this.fileProperties.load(stream);
        log.info("Initializing application properties from file finished:{}", fileProperties.toString());
    }

    private void initDbProperties() {
        try {
            Properties tempProp = new Properties();
            tempProp.putAll(propertyRepository.findAll().stream()
                    .collect(Collectors.toMap(AppProperty::getName, AppProperty::getValue)));
            if (!tempProp.isEmpty()) {
                dbPropertiesRef.set(tempProp);
            }
            log.info("Initializing application properties from database finished");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
