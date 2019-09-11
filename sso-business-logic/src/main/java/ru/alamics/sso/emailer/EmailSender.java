package ru.alamics.sso.emailer;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.email.DefaultEmailSenderProvider;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailSenderProvider;
import org.keycloak.theme.FreeMarkerException;
import ru.alamics.sso.util.FreeMarkerUtil;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import java.util.List;
import java.util.Map;

@Stateless
@Slf4j
@LocalBean
public class EmailSender {
    private EmailSenderProvider emailSenderProvider;

    public void send(EmailModel emailTemplate) throws EmailException {
        var realm = emailTemplate.getRealmModel();
        EmailTemplate template = processTemplate(emailTemplate.getSubjectAttributes(), emailTemplate.getBodyTemplate(), emailTemplate.getBodyAttributes());
        emailSenderProvider.send(realm.getSmtpConfig(), emailTemplate.getUser(), emailTemplate.getSubject(), template.getTextBody(), template.getHtmlBody());
    }

    @PostConstruct
    public void init() {
        this.emailSenderProvider = new DefaultEmailSenderProvider(null);
    }

    protected EmailTemplate processTemplate(List<Object> subjectAttributes, String template, Map<String, Object> attributes) throws EmailException {
        try {
            String textTemplate = String.format("templates/mail/text/%s", template);
            String textBody;
            try {
                textBody = FreeMarkerUtil.processTemplate(attributes, textTemplate);
            } catch (final FreeMarkerException e) {
                textBody = null;
            }
            String htmlTemplate = String.format("templates/mail/html/%s", template);
            String htmlBody;
            try {
                htmlBody = FreeMarkerUtil.processTemplate(attributes, htmlTemplate);
            } catch (final FreeMarkerException e) {
                htmlBody = null;
            }

            return new EmailTemplate(textBody, htmlBody);
        } catch (Exception e) {
            throw new EmailException("Failed to template email", e);
        }
    }

    protected static class EmailTemplate {

        private String textBody;
        private String htmlBody;

        public EmailTemplate(String textBody, String htmlBody) {
            this.textBody = textBody;
            this.htmlBody = htmlBody;
        }

        public String getTextBody() {
            return textBody;
        }

        public String getHtmlBody() {
            return htmlBody;
        }
    }
}
