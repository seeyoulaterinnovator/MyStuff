package ru.alamics.sso.keycloak.policy;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.policy.BlacklistPasswordPolicyProviderFactory;
import org.keycloak.policy.PasswordPolicyProvider;
import org.keycloak.policy.PasswordPolicyProviderFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class SsoBlacklistPasswordPolicyProviderFactory implements PasswordPolicyProviderFactory {

    protected static final String PROVIDER_ID = "ssoPasswordBlacklist";
    private static final String DISPLAY_NAME = "Sso Password Blacklist";

    private static final String SYSTEM_PROPERTY = "keycloak.password.blacklists.path";
    private static final String BLACKLISTS_PATH_PROPERTY = "blacklistsPath";

    private static final String JBOSS_SERVER_DATA_DIR = "jboss.server.data.dir";
    private static final String PASSWORD_BLACKLISTS_FOLDER = "password-blacklists/";

    private final ConcurrentMap<String, SsoFileBasedPasswordBlacklist> blacklistRegistry = new ConcurrentHashMap<>();

    private volatile Path blacklistsBasePath;

    private Config.Scope config;

    private static Path detectBlacklistsBasePath(Config.Scope config) {

        String pathFromSysProperty = System.getProperty(SYSTEM_PROPERTY);
        if (pathFromSysProperty != null) {
            return ensureExists(Paths.get(pathFromSysProperty));
        }

        String pathFromSpiConfig = config.get(BLACKLISTS_PATH_PROPERTY);
        if (pathFromSpiConfig != null) {
            return ensureExists(Paths.get(pathFromSpiConfig));
        }

        String pathServerDataDirProperty = System.getProperty(JBOSS_SERVER_DATA_DIR);
        if(pathServerDataDirProperty != null) {
            Path pathFromJbossDataPath = Paths.get(pathServerDataDirProperty).resolve(PASSWORD_BLACKLISTS_FOLDER);
            if (!Files.exists(pathFromJbossDataPath)) {
                if (!pathFromJbossDataPath.toFile().mkdirs()) {
                    log.error("Could not create folder for password blacklists: {}", pathFromJbossDataPath);
                    return null;
                }
            }
            return ensureExists(pathFromJbossDataPath);
        }
        return null;
    }

    private static Path ensureExists(Path path) {

        Objects.requireNonNull(path, "path");

        if (Files.exists(path)) {
            return path;
        }

        log.error("Password blacklists location does not exist: {}", path);

        return null;
    }

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
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public String getConfigType() {
        return PasswordPolicyProvider.STRING_CONFIG_TYPE;
    }

    @Override
    public String getDefaultConfigValue() {
        return "";
    }

    @Override
    public boolean isMultiplSupported() {
        return false;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    public BlacklistPasswordPolicyProviderFactory.PasswordBlacklist resolvePasswordBlacklist(String blacklistName) {

        Objects.requireNonNull(blacklistName, "blacklistName");

        String cleanedBlacklistName = blacklistName.trim();
        if (cleanedBlacklistName.isEmpty()) {
            throw new IllegalArgumentException("Password blacklist name must not be empty!");
        }

        return blacklistRegistry.computeIfAbsent(cleanedBlacklistName, (name) -> {
            SsoFileBasedPasswordBlacklist pbl = new SsoFileBasedPasswordBlacklist(this.blacklistsBasePath, name);
            pbl.lazyInit();
            return pbl;
        });
    }

}
