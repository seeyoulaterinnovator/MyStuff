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
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Singleton
@Startup
@DependsOn("ApplicationProperties")
public class InactiveBlockSchedule {
    @EJB
    private AutoLockNotificationRepository autoLockNotificationRepository;
    @EJB
    private UserRepository userRepository;
    @EJB
    private EmailSender sender;
    @EJB
    private RealmRepository realmRepository;
    @EJB
    private ApplicationProperties properties;

//    @Inject
//    @Property(value = "user.absence.blocking.days")
    private Integer absenceDaysBlock;

//    @Schedule(hour = "*/3", persistent = false)
    public void block () {
        final String DEBUG_STR = "block";
        log.info("start:{}", DEBUG_STR);
        final String subject = "Блокирование аккаунта";
        final String template = "block-notification.ftl";
        EmailModel.EmailModelBuilder emailTemplate = EmailModel.builder()
                .subject(subject)
                .bodyTemplate(template);
        List<UserEntity> usersToBlock = autoLockNotificationRepository.findNonBlockingUsers(absenceDaysBlock);
        List<AutoLockNotification> autoLockNotifications = usersToBlock.stream().map(user -> {
            var autoLockNotification = AutoLockNotification.builder()
                    .sendedAt(LocalDateTime.now())
                    .type(NotificationType.ABSENCE_BLOCKING)
                    .user(user)
                    .build();
            try {
                RealmModel realm = realmRepository.findRealmById(user.getRealmId());
                UserModel userModel = new UserAdapter(null, realm, null, user);
                emailTemplate.realmModel(realm)
                        .user(userModel);
                sender.send(emailTemplate.build());
            } catch (EmailException e) {
                log.error("{}: error={}", DEBUG_STR, e);
            }
            return autoLockNotification;
        }).collect(Collectors.toList());
        usersToBlock.forEach(userToBlock -> userToBlock.setEnabled(false));
        userRepository.save(usersToBlock);
        autoLockNotificationRepository.save(autoLockNotifications);

        log.info("stop:{}", DEBUG_STR);
    }


    @PostConstruct
    public void init() {
        this.absenceDaysBlock = Integer.parseInt(properties.getProperty("user.absence.blocking.days"));
    }
}
