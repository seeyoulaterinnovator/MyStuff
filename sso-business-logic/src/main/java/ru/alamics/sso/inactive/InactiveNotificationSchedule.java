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

import javax.annotation.PostConstruct;
import javax.ejb.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private Integer absenceDaysBlock;

    @Schedule(hour = "*/3", persistent = false)
    public void sendEmails() throws EmailException {
        final String DEBUG_STR = "sendEmails";
        log.info("start={}", DEBUG_STR);

        var bockNotification = bockNotification();
        var prepareBlockNotification = prepareBlockNotification();
        var autoLockNotifications = autoLockNotificationRepository.findNotifications();
        var usersToBlock = new ArrayList<UserEntity>();
        for(AutoLockNotification notification : autoLockNotifications) {
            var user = notification.getUser();
            RealmModel realm = realmRepository.findRealmById(user.getRealmId());
            UserModel userModel = new UserAdapter(null, realm, null, user);
            if(notification.getType() == NotificationType.ABSENCE_NOTIFICATION) {
                prepareBlockNotification.realmModel(realm)
                        .user(userModel);
                sender.send(prepareBlockNotification.build());
            } else if(notification.getType() == NotificationType.ABSENCE_BLOCKING) {
                bockNotification.realmModel(realm)
                        .user(userModel);
                sender.send(bockNotification.build());
                user.setEnabled(false);
                usersToBlock.add(user);
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
        Map<String, Object> body = new HashMap<>();
        body.put("absence", absenceDaysBlock);

        return EmailModel.builder()
                .bodyAttributes(body)
                .subject(subject)
                .bodyTemplate(template);
    }

    @PostConstruct
    public void init() {
        this.absenceDaysBlock = Integer.parseInt(properties.getProperty("user.absence.blocking.days"));
    }
}
