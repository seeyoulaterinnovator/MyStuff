package ru.alamics.sso.keycloak.policy;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.policy.BlacklistPasswordPolicyProviderFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class SsoFileBasedPasswordBlacklist implements BlacklistPasswordPolicyProviderFactory.PasswordBlacklist {
    @Getter
    private final String name;

    private final Path path;

    private Set<String> blacklist;

    public SsoFileBasedPasswordBlacklist(@Nullable Path blacklistBasePath, String name) {
        this.name = name;

        if(blacklistBasePath == null) {
            log.error("Password blacklist path empty");
            path = null;
            return;
        }

        this.path = blacklistBasePath.resolve(name);

        if (name.contains("/")) {
            throw new IllegalArgumentException(name + " must not contain slashes!");
        }

        if (!Files.exists(this.path)) {
            log.error("Password blacklist {} not found!", name);
        }
    }

    public boolean contains(String password) {
        return blacklist != null && blacklist.contains(password);
    }

    @Synchronized
    public void lazyInit() {
        if (blacklist == null && path != null) {
            try(Stream<String> stream = Files.lines(path)) {
                blacklist = stream.collect(Collectors.toSet());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
