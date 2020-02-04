package ru.alamics.sso.schedule;


import lombok.extern.slf4j.Slf4j;
import org.keycloak.common.util.Time;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.jpa.AdminEventEntity;
import org.keycloak.models.PasswordPolicy;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.UserAdapter;
import org.keycloak.models.jpa.entities.ClientEntity;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.util.JsonSerialization;
import ru.alamics.sso.emailer.EmailModel;
import ru.alamics.sso.emailer.EmailSender;
import ru.alamics.sso.jpa.entity.AutoLockNotification;
import ru.alamics.sso.jpa.entity.common.NotificationType;
import ru.alamics.sso.jpa.repository.*;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.registration.mapper.DataMapper;
import ru.alamics.sso.settings.SettingConstants;
import ru.alamics.sso.settings.SettingsDto;
import ru.alamics.sso.settings.SettingsService;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Startup
@Singleton
@DependsOn("ApplicationProperties")
public class UserSchedule {
    private static final String TIMER_NAME = "User Schedule Timer";
    private static final long DEFAULT_INTERVAL_DURATION = 300000;
    private final static String[] SETTINGS_REALM_NAMES_SCHEDULE = {"user", "manager"};
    private final static String CLIENT_ID = "lkb2b";
    private final static String TIMER_INTERVAL_DURATION_PROPERTY = "application.schedule.user.milliseconds";
    @EJB
    private EmailSender sender;
    @EJB
    private PolicyRepository policyRepository;
    @EJB
    private AutoLockNotificationRepository autoLockNotificationRepository;
    @EJB
    private UserHistoryLoginRepository userHistoryLoginRepository;
    @EJB
    private RealmRepository realmRepository;
    @EJB
    private ClientRepository clientRepository;
    @EJB
    private AdminEventRepository adminEventRepository;
    @EJB
    private ApplicationProperties properties;
    @EJB
    private SettingsService settingsService;
    @Resource
    private TimerService timerService;

    @PostConstruct
    private void init() {
        final TimerConfig timerConfig = new TimerConfig(TIMER_NAME, false);
        try {
            final long intervalDuration = Long.parseLong(properties.getProperty(TIMER_INTERVAL_DURATION_PROPERTY));
            timerService.createIntervalTimer(intervalDuration, intervalDuration, timerConfig);
            log.info("Timer:{} is created, interval duration set to value={} milliseconds ", TIMER_NAME, intervalDuration);
        } catch (Exception e) {
            timerService.createIntervalTimer(DEFAULT_INTERVAL_DURATION, DEFAULT_INTERVAL_DURATION, timerConfig);
            log.warn("Timer:{} is created; Error read configuration, interval duration set to default value={} milliseconds",
                    TIMER_NAME, DEFAULT_INTERVAL_DURATION);
        }
    }

    @Timeout
    public void schedule(Timer timer) {
        if (!TIMER_NAME.equals(timer.getInfo().toString())) {
            return;
        }

        findExpiredPassword();
        for (String realm : SETTINGS_REALM_NAMES_SCHEDULE) {
            notificationInactiveUsers(realm);
            block(realm);
        }
        sendEmails();
    }

    private void notificationInactiveUsers(String realm) {
        final String DEBUG_STR = "findNotifications";
        log.debug("start:{}", DEBUG_STR);
        long absenceTimeNotification = settingsService.getSettingsValue(SettingConstants.ABSENCE_NOTIFICATION_DAYS, realm);
        if (absenceTimeNotification > -1) {
            userHistoryLoginRepository.findInactiveUsers(absenceTimeNotification, realm);
        }
        log.debug("stop:{}", DEBUG_STR);
    }

    private void block(String realm) {
        final String DEBUG_STR = "block";
        log.debug("start:{}", DEBUG_STR);
        long absenceTimeBlock = settingsService.getSettingsValue(SettingConstants.ABSENCE_BLOCKING_DAYS, realm) -
                settingsService.getSettingsValue(SettingConstants.ABSENCE_NOTIFICATION_DAYS, realm);
        if (absenceTimeBlock > -1) {
            autoLockNotificationRepository.findUsersToBlock(absenceTimeBlock, realm);
        }
        log.debug("stop:{}", DEBUG_STR);
    }

    private void findExpiredPassword() {
        final String DEBUG_STR = "findExpiredPassword";
        log.debug("start: {}", DEBUG_STR);
        var realms = policyRepository.findRealmWithPolicy(PasswordPolicy.FORCE_EXPIRED_ID);

        realms.forEach(realm -> {
            String passwordPolicy = realm.getPasswordPolicy();
            if (Objects.nonNull(passwordPolicy)) {
                var charNumbs = PasswordPolicy.FORCE_EXPIRED_ID.length() + 3;
                var index = passwordPolicy.indexOf(PasswordPolicy.FORCE_EXPIRED_ID);
                var expirePolicy = passwordPolicy.substring(index, index + charNumbs);
                int expiresDays = Integer.parseInt(expirePolicy.substring(expirePolicy.indexOf('(') + 1, expirePolicy.lastIndexOf(')')));
                if (expiresDays != -1) {
                    long timeToExpire = TimeUnit.DAYS.toMillis(expiresDays);
                    policyRepository.findExpiredPasswords(realm.getId(), timeToExpire);
                }
            }
        });
        log.debug("stop: {}", DEBUG_STR);
    }

    private void sendEmails() {
        final String DEBUG_STR = "sendEmails";
        log.debug("start={}", DEBUG_STR);

        var autoLockNotifications = autoLockNotificationRepository.findNotifications();
        for (AutoLockNotification notification : autoLockNotifications) {
            var user = notification.getUser();
            RealmModel realm = realmRepository.findRealmById(user.getRealmId());
            ClientEntity client = clientRepository.findClientById(CLIENT_ID, realm.getName());
            UserModel userModel = new UserAdapter(null, realm, null, user);
            if (notification.getType() == NotificationType.ABSENCE_NOTIFICATION) {
                var prepareBlockNotification = prepareBlockNotification(realm.getName(), getClientLink(client));
                prepareBlockNotification.realmModel(realm)
                        .user(userModel);
                sender.blockingSend(prepareBlockNotification.build());
            } else if (notification.getType() == NotificationType.ABSENCE_BLOCKING) {
                var bockNotification = bockNotification();
                bockNotification.realmModel(realm)
                        .user(userModel);
                sender.blockingSend(bockNotification.build());
                user.setEnabled(false);
                createAdminEvent(OperationType.UPDATE, user, realm);
            } else if (notification.getType() == NotificationType.PASSWORD_EXPIRED) {
                var passwordExpired = passwordExpired(getClientLink(client));
                passwordExpired.realmModel(realm)
                        .user(userModel);
                sender.blockingSend(passwordExpired.build());
            }
        }
        log.debug("stop={}", DEBUG_STR);
    }


    private EmailModel.EmailModelBuilder bockNotification() {
        final String subject = "Блокирование аккаунта";
        final String template = "block-notification.ftl";
        return EmailModel.builder()
                .subject(subject)
                .bodyTemplate(template);
    }

    private EmailModel.EmailModelBuilder prepareBlockNotification(String realm, String link) {
        final String subject = "Предупреждение о блокирование аккаунта";
        final String template = "block-prepare-notification.ftl";
        SettingsDto blockSetting = settingsService.getSetting(SettingConstants.ABSENCE_BLOCKING_DAYS, realm);
        long inactiveBlockTimeout = settingsService.getSettingsValue(SettingConstants.ABSENCE_BLOCKING_DAYS, realm);
        long inactiveNotificationTimeout = settingsService.getSettingsValue(SettingConstants.ABSENCE_NOTIFICATION_DAYS, realm);
        String timeToBlock = String.valueOf(
                blockSetting.getUnit().convert(inactiveBlockTimeout - inactiveNotificationTimeout, TimeUnit.SECONDS));
        Map<String, Object> body = new HashMap<>();
        body.put("absence", timeToBlock + " " + Translator.getRusTranslateTimeUnit(timeToBlock, blockSetting.getUnit()));
        body.put("link", link);
        return EmailModel.builder()
                .bodyAttributes(body)
                .subject(subject)
                .bodyTemplate(template);
    }

    private EmailModel.EmailModelBuilder passwordExpired(String link) {
        final String subject = "Истек срок жизни пароля";
        final String template = "password-expires.ftl";
        Map<String, Object> body = new HashMap<>();
        body.put("link", link);
        return EmailModel.builder()
                .bodyAttributes(body)
                .subject(subject)
                .bodyTemplate(template);
    }

    private String getClientLink(ClientEntity client) {
        if (client != null) {
            return client.getRedirectUris().stream().findFirst().get();
        }
        return "";
    }

    private void createAdminEvent(OperationType operationType, UserEntity userEntity, RealmModel realm) {
        AdminEventEntity adminEvent = new AdminEventEntity();
        adminEvent.setTime(Time.toMillis(Time.currentTime()));
        adminEvent.setRealmId(realm.getName());
        adminEvent.setOperationType(operationType.name());
        adminEvent.setAuthRealmId(realm.getName());
        adminEvent.setResourcePath("autoblock");
        try {
            adminEvent.setRepresentation(JsonSerialization.writeValueAsString(DataMapper.toUserEntityRepresentation(userEntity)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        adminEvent.setResourceType("USER");
        adminEventRepository.save(adminEvent);
    }
}
