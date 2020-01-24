package ru.alamics.sso.emailer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.common.util.Time;
import org.keycloak.email.DefaultEmailSenderProvider;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailSenderProvider;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.jpa.AdminEventEntity;
import org.keycloak.theme.FreeMarkerException;
import org.keycloak.theme.FreeMarkerUtil;
import org.keycloak.theme.Theme;
import org.keycloak.theme.beans.MessageFormatterMethod;
import org.keycloak.util.JsonSerialization;
import ru.alamics.sso.keycloak.repository.AdminEventRepository;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.util.CustomFreeMarkerUtil;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Stateless
@Slf4j
@LocalBean
public class EmailSender {
    private static final String SEND_INTERVAL_PROPERTY = "emailSender.interval.milliseconds";

    private long sendInterval = 1000;

    private FreeMarkerUtil freeMarkerUtil;
    private ConcurrentLinkedQueue<EmailModel> emailQueue;
    private EmailSenderProvider emailSenderProvider;
    private ExecutorService executorService;
    private Future sendTask;
    @EJB
    private AdminEventRepository adminEventRepository;
    @EJB
    private ApplicationProperties properties;

    public void send(EmailModel emailModel) {
        if (emailModel.getUser().getEmail() == null) {
            return;
        }
        emailQueue.offer(emailModel);

        if (sendTask == null || sendTask.isDone()) {
            sendTask = executorService.submit(() -> {
                log.info("Thread email sender is started");
                while (!emailQueue.isEmpty()) {
                    send();
                    try {
                        Thread.sleep(sendInterval);
                    } catch (InterruptedException e) {
                    }
                }
                log.info("Thread email sender is finished");
            });
        }
    }

    private void send() {
        try {
            EmailModel emailModel = emailQueue.poll();
            EmailTemplate template = processTemplate(emailModel.getSubject(), emailModel.getSubjectAttributes(),
                    emailModel.getBodyTemplate(), emailModel.getBodyAttributes(),
                    emailModel.getTheme(), emailModel.getLocale());
            emailSenderProvider.send(emailModel.getRealmModel().getSmtpConfig(), emailModel.getUser(), template.getSubject(), template.getTextBody(), template.getHtmlBody());
            createEmailEvent(OperationType.ACTION, emailModel, template.subject);
            log.info("send to " + emailModel.getUser().getEmail() + " is finished");
        } catch (Exception e) {
            log.error("Thread email sender is ended with error:", e);
        }
    }

    @PostConstruct
    public void init() {
        this.emailSenderProvider = new DefaultEmailSenderProvider(null);
        this.emailQueue = new ConcurrentLinkedQueue<EmailModel>();
        this.freeMarkerUtil = new FreeMarkerUtil();
        this.executorService = Executors.newSingleThreadExecutor();

        try {
            sendInterval = Long.parseLong(properties.getProperty(SEND_INTERVAL_PROPERTY));
        } finally {
            log.info("send interval={} milliseconds", sendInterval);
        }
    }

    private void createEmailEvent(OperationType operationType, EmailModel emailModel, String emailTheme) {
        AdminEventEntity adminEvent = new AdminEventEntity();
        adminEvent.setTime(Time.toMillis(Time.currentTime()));
        adminEvent.setRealmId(emailModel.getRealmModel().getName());
        adminEvent.setOperationType(operationType.name());
        adminEvent.setAuthRealmId(emailModel.getRealmModel().getName());
        adminEvent.setResourcePath("sending_email/" + emailModel.getUser().getId());
        Map<String, Object> repr = new HashMap<>();
        repr.put("action", "send_account_data");
        repr.put("userId", emailModel.getUser().getId());
        repr.put("email", emailModel.getUser().getEmail());
        repr.put("emailTheme", emailTheme);
        try {
            adminEvent.setRepresentation(JsonSerialization.writeValueAsString(repr));
        } catch (IOException e) {
            log.error("Representation for email event is failed", e);
        }
        adminEvent.setResourceType("USER");
        adminEventRepository.save(adminEvent);
    }

    protected EmailTemplate processTemplate(String subjectKey, List<Object> subjectAttributes, String template, Map<String, Object> attributes,
                                            Theme theme, Locale locale) throws EmailException {
        try {
            String textBody;
            String subject = null;
            if (locale != null) {
                attributes.put("locale", locale);
                Properties rb = theme.getMessages(locale);
                attributes.put("msg", new MessageFormatterMethod(locale, rb));
                subject = new MessageFormat(rb.getProperty(subjectKey, subjectKey), locale).format(subjectAttributes.toArray());
            }
            if (theme != null) {
                attributes.put("properties", theme.getProperties());
            }
            String textTemplate = String.format("/text/%s", template);
            try {
                if (theme == null) {
                    textBody = CustomFreeMarkerUtil.processTemplate(attributes, textTemplate);
                } else {
                    textBody = freeMarkerUtil.processTemplate(attributes, textTemplate, theme);
                }
            } catch (final FreeMarkerException e) {
                textBody = null;
            }
            String htmlTemplate = String.format("/html/%s", template);
            String htmlBody;
            try {
                if (theme == null) {
                    htmlBody = CustomFreeMarkerUtil.processTemplate(attributes, htmlTemplate);
                } else {
                    htmlBody = freeMarkerUtil.processTemplate(attributes, htmlTemplate, theme);
                }
            } catch (final FreeMarkerException e) {
                htmlBody = null;
            }

            return new EmailTemplate(subject, textBody, htmlBody);
        } catch (Exception e) {
            throw new EmailException("Failed to template email", e);
        }
    }

    @Data
    @AllArgsConstructor
    @Getter
    protected static class EmailTemplate {
        private String subject;
        private String textBody;
        private String htmlBody;
    }
}
