package ru.alamics.sso.inactive;


import lombok.extern.slf4j.Slf4j;
import org.keycloak.email.EmailException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.UserAdapter;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.emailer.EmailModel;
import ru.alamics.sso.emailer.EmailSender;
import ru.alamics.sso.keycloak.entity.AutoLockNotification;
import ru.alamics.sso.keycloak.entity.common.NotificationType;
import ru.alamics.sso.keycloak.repository.AutoLockNotificationRepository;
import ru.alamics.sso.keycloak.repository.RealmRepository;
import ru.alamics.sso.keycloak.repository.UserRepository;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.property.PropertyConstants;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Singleton
@Startup
@DependsOn("ApplicationProperties")
public class InactiveNotificationSchedule {
    @EJB
    private EmailSender sender;
    @EJB
    private UserRepository userRepository;
    @EJB
    private AutoLockNotificationRepository autoLockNotificationRepository;
    @EJB
    private RealmRepository realmRepository;
    @EJB
    private ApplicationProperties properties;

    private String host;

    @Schedule(hour = "*/2", persistent = false)
    public void sendEmails() throws EmailException {
        final String DEBUG_STR = "sendEmails";
        log.info("start={}", DEBUG_STR);

        var autoLockNotifications = autoLockNotificationRepository.findNotifications();
        var usersToBlock = new ArrayList<UserEntity>();
        for(AutoLockNotification notification : autoLockNotifications) {
            var user = notification.getUser();
            RealmModel realm = realmRepository.findRealmById(user.getRealmId());
            UserModel userModel = new UserAdapter(null, realm, null, user);
            if(notification.getType() == NotificationType.ABSENCE_NOTIFICATION) {
                var prepareBlockNotification = prepareBlockNotification();
                prepareBlockNotification.realmModel(realm)
                        .user(userModel);
                sender.send(prepareBlockNotification.build());
            } else if(notification.getType() == NotificationType.ABSENCE_BLOCKING) {
                var bockNotification = bockNotification();
                bockNotification.realmModel(realm)
                        .user(userModel);
                sender.send(bockNotification.build());
                user.setEnabled(false);
                usersToBlock.add(user);
            } else if(notification.getType() == NotificationType.PASSWORD_EXPIRED) {
                var passwordExpired = passwordExpired();
                passwordExpired.realmModel(realm)
                        .user(userModel);
                sender.send(passwordExpired.build());
            }
        }
        userRepository.save(usersToBlock);

        log.info("stop={}", DEBUG_STR);
    }


    private EmailModel.EmailModelBuilder bockNotification() {
        final String subject = "Блокирование аккаунта";
        final String template = "block-notification.ftl";
        return EmailModel.builder()
                .subject(subject)
                .bodyTemplate(template);
    }

    private EmailModel.EmailModelBuilder prepareBlockNotification() {
        final String subject = "Предупреждение о блокирование аккаунта";
        final String template = "block-prepare-notification.ftl";
        Long absenceTimeBlock = Long.parseLong(properties.getProperty(PropertyConstants.ABSENCE_BLOCKING_DAYS, "user"));
        Map<String, Object> body = new HashMap<>();
        body.put("absence", absenceTimeBlock);

        return EmailModel.builder()
                .bodyAttributes(body)
                .subject(subject)
                .bodyTemplate(template);
    }

    private EmailModel.EmailModelBuilder passwordExpired() {
        final String subject = "Истек срок жизни пароля";
        final String template = "password-expires.ftl";
        Map<String, Object> body = new HashMap<>();
        String state = "0/" + UUID.randomUUID();
        String auth = String.format("%s/auth/realms/user/protocol/openid-connect/auth?client_id=account", host);
        String redirectUri = String.format("%s/auth/realms/user/account/login-redirect", host);
        body.put("link", String.format("%s&redirect_uri=%s&state=%s&response_type=code", auth, URLEncoder.encode(redirectUri, StandardCharsets.UTF_8), state));

        return EmailModel.builder()
                .bodyAttributes(body)
                .subject(subject)
                .bodyTemplate(template);
    }

    @PostConstruct
    public void init() {
        this.host = properties.getProperty("application.host");
    }
}
