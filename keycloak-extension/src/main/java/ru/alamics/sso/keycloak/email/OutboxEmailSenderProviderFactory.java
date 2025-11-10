package ru.alamics.sso.keycloak.email;

import org.keycloak.Config;
import org.keycloak.email.EmailSenderProvider;
import org.keycloak.email.EmailSenderProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class OutboxEmailSenderProviderFactory implements EmailSenderProviderFactory {
    public static final String PROVIDER_ID = "outbox";

    @Override
    public EmailSenderProvider create(KeycloakSession session) {
        return new OutboxEmailSenderProvider(session);
    }

    @Override
    public void init(Config.Scope config) {
        // no-op
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // no-op
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
