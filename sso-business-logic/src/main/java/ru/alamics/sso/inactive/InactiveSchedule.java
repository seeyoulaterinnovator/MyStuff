package ru.alamics.sso.inactive;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.keycloak.entity.AutoLockNotification;
import ru.alamics.sso.keycloak.entity.common.NotificationType;
import ru.alamics.sso.keycloak.repository.AutoLockNotificationRepository;
import ru.alamics.sso.keycloak.repository.UserHistoryLoginRepository;
import ru.alamics.sso.property.Property;

import javax.ejb.Singleton;
import javax.ejb.Startup;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Startup
@Slf4j
@Singleton
public class InactiveSchedule {

    private UserHistoryLoginRepository repository;
    private AutoLockNotificationRepository autoLockNotificationRepository;
    private KeycloakSession session;
    private EmailTemplateProvider emailTemplate;

    @Inject
    @Property("user.absence.days")
    private int absenceDaysNotification;

    private int absenceDaysBlock;

//    @Schedule(hour = "*", second = "*/10", minute = "*", persistent = false)
    public void notificationInactiveUsers() {
        final String DEBUG_STR = "findInactiveUsers";
        log.info("start:{}", DEBUG_STR);
        List<UserEntity> users = repository.findInactiveUsers(absenceDaysNotification);
        String subject = "emailAccountDataSubject";
        String template = "block-prepare-notification.ftl";
        users.forEach(user -> {

        });

        List<AutoLockNotification> autoLockNotifications = users.stream().map(user -> {
            AutoLockNotification autoLockNotification = AutoLockNotification.builder()
                    .sendedAt(LocalDateTime.now())
                    .type(NotificationType.ABSENCE_NOTIFICATION)
                    .user(user)
                    .build();
//            emailTemplate.setRealm().send(subject, template, null);
            return autoLockNotification;
        }).collect(Collectors.toList());
        autoLockNotificationRepository.save(autoLockNotifications);

        log.info("stop:{}", DEBUG_STR);
    }

//    @Schedule
    public void block() {
      final String DEBUG_STR = "block";
      log.info("start:{}", DEBUG_STR);

      List<UserEntity> usersToBlock = autoLockNotificationRepository.findNonBlockingUsers(absenceDaysBlock);
        List<AutoLockNotification> autoLockNotifications = usersToBlock.stream().map(user -> {
            //            emailTemplate.setRealm().send(subject, template, null);
            return AutoLockNotification.builder()
                    .sendedAt(LocalDateTime.now())
                    .type(NotificationType.ABSENCE_BLOCKING)
                    .user(user)
                    .build();
        }).collect(Collectors.toList());

      log.info("stop:{}", DEBUG_STR);
    }
//    @PostConstruct
//    private void init() {
//        if(this.session == null) {
//            DefaultKeycloakSessionFactory factory = new DefaultKeycloakSessionFactory();
//            factory.init();
//            try {
//                this.session = factory.create();
//                this.repository = RepositoryFactory.create(session);
//                this.emailTemplate = session.getProvider(EmailTemplateProvider.class);
//            } finally {
//                this.session.close();
//            }
//        }
//    }
}
