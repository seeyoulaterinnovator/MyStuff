package ru.alamics.sso.keycloak.policy;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.policy.BlacklistPasswordPolicyProviderFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SsoFileBasedPasswordBlacklist implements BlacklistPasswordPolicyProviderFactory.PasswordBlacklist {

    private static final double FALSE_POSITIVE_PROBABILITY = 0.01;

    private static final int BUFFER_SIZE_IN_BYTES = 512 * 1024;

    private final String name;

    private final Path path;

    private BloomFilter<String> blacklist;

    private List<String> customBlackList = new ArrayList<>();

    public SsoFileBasedPasswordBlacklist(Path blacklistBasePath, String name) {

        this.name = name;
        this.path = blacklistBasePath.resolve(name);


        if (name.contains("/")) {
            throw new IllegalArgumentException("" + name + " must not contain slashes!");
        }

        if (!Files.exists(this.path)) {
            throw new IllegalArgumentException("Password blacklist " + name + " not found!");
        }
    }

    private static BufferedReader newReader(Path path) throws IOException {
        return new BufferedReader(Files.newBufferedReader(path), BUFFER_SIZE_IN_BYTES);
    }

    public String getName() {
        return name;
    }

    public boolean contains(String password) {
        return (blacklist != null && blacklist.mightContain(password)) ||
                (!customBlackList.isEmpty() && customBlackList.contains(password));
    }

    void lazyInit() {

        if (blacklist != null) {
            return;
        }

        this.blacklist = load();
    }

    private BloomFilter<String> load() {

        try {
            log.info("Loading blacklist with name {} from {} - start", name, path);

            long passwordCount = getPasswordCount();

            BloomFilter<String> filter = BloomFilter.create(
                    Funnels.stringFunnel(StandardCharsets.UTF_8),
                    passwordCount,
                    FALSE_POSITIVE_PROBABILITY);

            try (BufferedReader br = newReader(path)) {
                br.lines().forEach(pass -> {
                    filter.put(pass);
                    customBlackList.add(pass);
                });
            }

            log.info("Loading blacklist with name {} from {} - end", name, path);

            return filter;
        } catch (IOException e) {
            throw new RuntimeException("Could not load password blacklist from path: " + path, e);
        }
    }

    private long getPasswordCount() throws IOException {
        try (BufferedReader br = newReader(path)) {
            return br.lines().count();
        }
    }
}
