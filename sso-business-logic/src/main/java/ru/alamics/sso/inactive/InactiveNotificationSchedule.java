package ru.alamics.sso.inactive;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.email.EmailException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.UserAdapter;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.emailer.EmailSender;
import ru.alamics.sso.emailer.EmailModel;
import ru.alamics.sso.keycloak.entity.AutoLockNotification;
import ru.alamics.sso.keycloak.entity.common.NotificationType;
import ru.alamics.sso.keycloak.repository.AutoLockNotificationRepository;
import ru.alamics.sso.keycloak.repository.RealmRepository;
import ru.alamics.sso.keycloak.repository.UserHistoryLoginRepository;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.property.Property;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import javax.inject.Inject;


import java.time.LocalDateTime;
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
    private UserHistoryLoginRepository userHistoryLoginRepository;
    @EJB
    private AutoLockNotificationRepository autoLockNotificationRepository;
    @EJB
    private EmailSender sender;
    @EJB
    private RealmRepository realmRepository;
    @EJB
    private ApplicationProperties properties;


    //TODO довести до ума inject пропертей
//    @Inject
//    @Property(value = "user.absence.notifications.days")
    private Integer absenceDaysNotification;
    private Integer absenceDaysBlock;

//    @Schedule(hour = "*/3", persistent = false)
    public void notificationInactiveUsers () {
        final String DEBUG_STR = "findInactiveUsers";
        log.info("start:{}", DEBUG_STR);
        final String subject = "Предупреждение о блокирование аккаунта";
        final String template = "block-prepare-notification.ftl";
        List<UserEntity> users = userHistoryLoginRepository.findInactiveUsers(absenceDaysNotification);
        Map<String, Object> body = new HashMap<>();
        body.put("absence", absenceDaysBlock);
        EmailModel.EmailModelBuilder emailTemplate = EmailModel.builder()
                .bodyAttributes(body)
                .subject(subject)
                .bodyTemplate(template);

        List<AutoLockNotification> autoLockNotifications = users.stream().map(user -> {
            var autoLockNotification = AutoLockNotification.builder()
                    .sendedAt(LocalDateTime.now())
                    .type(NotificationType.ABSENCE_NOTIFICATION)
                    .user(user)
                    .build();
            RealmModel realm = realmRepository.findRealmById(user.getRealmId());
            UserModel userModel = new UserAdapter(null, realm, null, user);
            emailTemplate.realmModel(realm)
                    .user(userModel);
            try {
                sender.send(emailTemplate.build());
            } catch (EmailException e) {
                log.error("{}: error={}", DEBUG_STR, e);
            }
            return autoLockNotification;
        }).collect(Collectors.toList());
        autoLockNotificationRepository.save(autoLockNotifications);

        log.info("stop:{}", DEBUG_STR);
    }


    @PostConstruct
    public void init() {
        this.absenceDaysNotification = Integer.parseInt(properties.getProperty("user.absence.notifications.days"));
        this.absenceDaysBlock = Integer.parseInt(properties.getProperty("user.absence.blocking.days"));
    }
}
