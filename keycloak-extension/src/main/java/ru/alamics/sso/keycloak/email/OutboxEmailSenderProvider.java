package ru.alamics.sso.keycloak.email;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.email.DefaultEmailSenderProvider;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailSenderProvider;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.property.ApplicationProperties;

import java.util.Map;

/**
 * Email sender provider that respects the global emailSender.dont.send flag.
 */
@Slf4j
public class OutboxEmailSenderProvider implements EmailSenderProvider {
    static final String DONT_SEND_PROPERTY = "emailSender.dont.send";

    private final ApplicationProperties properties;
    private final EmailSenderProvider delegate;

    public OutboxEmailSenderProvider(KeycloakSession session) {
        this.properties = Lookup.lookup(ApplicationProperties.class);
        this.delegate = new DefaultEmailSenderProvider(session);
    }

    @Override
    public void send(Map<String, String> config, String address, String subject, String text, String html) throws EmailException {
        doSend(config, address, subject, text, html);
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public void send(Map<String, String> config, org.keycloak.models.UserModel user, String subject, String text, String html) throws EmailException {
        String email = user != null ? user.getEmail() : null;
        doSend(config, email, subject, text, html);
    }

    private void doSend(Map<String, String> config, String email, String subject, String text, String html) throws EmailException {
        boolean dontSend = Boolean.parseBoolean(properties.getProperty(DONT_SEND_PROPERTY, "false"));
        if (dontSend) {
            log.warn("Skip sending email to {} due to {}=true", email, DONT_SEND_PROPERTY);
            throw new EmailException("Sending disabled by " + DONT_SEND_PROPERTY + "=true");
        }

        try {
            delegate.send(config, email, subject, text, html);
        } catch (EmailException e) {
            log.error("Failed to send email to {}: {}", email, e.getMessage(), e);
            throw e;
        } catch (RuntimeException e) {
            log.error("Unexpected error while sending email to {}", email, e);
            throw new EmailException("Failed to send email", e);
        }
    }
}
