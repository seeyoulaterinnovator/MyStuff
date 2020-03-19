package ru.alamics.sso.keycloak.policy;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.policy.BlacklistPasswordPolicyProvider;
import org.keycloak.policy.BlacklistPasswordPolicyProviderFactory;
import org.keycloak.policy.PasswordPolicyProvider;
import org.keycloak.policy.PasswordPolicyProviderFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@Slf4j
public class SsoBlacklistPasswordPolicyProviderFactory extends BlacklistPasswordPolicyProviderFactory implements PasswordPolicyProviderFactory {

    public static final String ID = "ssoPasswordBlacklist";

    private volatile Path blacklistsBasePath;

    private Config.Scope config;

    @Override
    public PasswordPolicyProvider create(KeycloakSession session) {
        if (this.blacklistsBasePath == null) {
            synchronized (this) {
                if (this.blacklistsBasePath == null) {
                    this.blacklistsBasePath = detectBlacklistsBasePath(config);
                }
            }
        }
        return new SsoBlacklistPasswordPolicyProvider(session.getContext(), this);
    }

    @Override
    public void init(Config.Scope config) {
        this.config = config;
    }

    @Override
    public String getDisplayName() {
        return "Sso Password Blacklist";
    }

    @Override
    public String getId() {
        return ID;
    }

    private static Path detectBlacklistsBasePath(Config.Scope config) {

        String pathFromSysProperty = System.getProperty(SYSTEM_PROPERTY);
        if (pathFromSysProperty != null) {
            return ensureExists(Paths.get(pathFromSysProperty));
        }

        String pathFromSpiConfig = config.get(BLACKLISTS_PATH_PROPERTY);
        if (pathFromSpiConfig != null) {
            return ensureExists(Paths.get(pathFromSpiConfig));
        }

        String pathFromJbossDataPath = System.getProperty(JBOSS_SERVER_DATA_DIR) + "/" + PASSWORD_BLACKLISTS_FOLDER;
        if (!Files.exists(Paths.get(pathFromJbossDataPath))) {
            if (!Paths.get(pathFromJbossDataPath).toFile().mkdirs()) {
                log.error("Could not create folder for password blacklists: {}", pathFromJbossDataPath);
            }
        }
        return ensureExists(Paths.get(pathFromJbossDataPath));
    }
    private static Path ensureExists(Path path) {

        Objects.requireNonNull(path, "path");

        if (Files.exists(path)) {
            return path;
        }

        throw new IllegalStateException("Password blacklists location does not exist: " + path);
    }

}
