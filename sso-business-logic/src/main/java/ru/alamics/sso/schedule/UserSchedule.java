package ru.alamics.sso.schedule;

import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.*;
import jakarta.ws.rs.core.Context;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.common.util.Time;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.jpa.AdminEventEntity;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.PasswordPolicy;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.UserAdapter;
import org.keycloak.models.jpa.entities.ClientEntity;
import org.keycloak.models.jpa.entities.RealmEntity;
import org.keycloak.models.jpa.entities.UserAttributeEntity;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.services.scheduled.ClusterAwareScheduledTaskRunner;
import org.keycloak.timer.ScheduledTask;
import org.keycloak.timer.TimerProvider;
import org.keycloak.util.JsonSerialization;
import ru.alamics.sso.client.ClientService;
import ru.alamics.sso.emailer.EmailModel;
import ru.alamics.sso.emailer.EmailSender;
import ru.alamics.sso.jpa.entity.AutoLockNotification;
import ru.alamics.sso.jpa.entity.common.BlockType;
import ru.alamics.sso.jpa.entity.common.NotificationType;
import ru.alamics.sso.jpa.repository.*;
import ru.alamics.sso.keycloak.GeneralRealm;
import ru.alamics.sso.registration.mapper.DataMapper;
import ru.alamics.sso.settings.SettingConstants;
import ru.alamics.sso.settings.SettingsDto;
import ru.alamics.sso.settings.SettingsService;
import ru.alamics.sso.util.E2EUtil;
import ru.alamics.sso.util.Util;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
@Slf4j
public class UserSchedule implements ScheduledTask {
    private static final String TIMER_NAME = "User Schedule Timer";
    private static final long DEFAULT_INTERVAL_DURATION = 300000;
    private final static String DEFAULT_CLIENT_ID = "account";

    @Inject
    EmailSender sender;
    @Inject
    PolicyRepository policyRepository;
    @Inject
    AutoLockNotificationRepository autoLockNotificationRepository;
    @Inject
    UserHistoryLoginRepository userHistoryLoginRepository;
    @Inject
    RealmRepository realmRepository;
    @Inject
    ClientRepository clientRepository;
    @Inject
    AdminEventRepository adminEventRepository;
    @Inject
    SettingsService settingsService;
    @Inject
    ClientService clientService;
    @Inject
    UserRepository userRepository;

    @Context
    KeycloakSession session;

    TimerProvider timerProvider;

    @PostConstruct
    void init() {
        timerProvider = session.getProvider(TimerProvider.class);
    }

    void onStart(@Observes StartupEvent ev) {
        changeScheduleTimer();
    }

    public void changeScheduleTimer() {
        long intervalDuration = DEFAULT_INTERVAL_DURATION;
        long lockDuration = 0;
        try {
            intervalDuration = settingsService.getSettingsLongValue(
                    SettingConstants.TIMER_INTERVAL_DURATION_PROPERTY,
                    GeneralRealm.MASTER
            );
        } catch (Exception e) {
            if(E2EUtil.isE2E()) {
                log.error(e.getMessage());
            } else {
                throw e;
            }
        }
        try {
            lockDuration = settingsService.getSettingsLongValue(
                    SettingConstants.TIMER_LOCK_DURATION_PROPERTY,
                    GeneralRealm.MASTER
            );
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
        if (intervalDuration <= 0) {
            intervalDuration = DEFAULT_INTERVAL_DURATION;
        }
        if(lockDuration <= 0) {
            lockDuration = intervalDuration * 5;
        }
        intervalDuration *= 1000;
        lockDuration *= 1000;

        timerProvider.cancelTask(TIMER_NAME);
        timerProvider.schedule(
                new ClusterAwareScheduledTaskRunner(session.getKeycloakSessionFactory(), this, lockDuration),
                intervalDuration
        );
        log.info("Timer: {} is created, interval duration value = {} ms, lock duration value = {} ms ",
                TIMER_NAME, intervalDuration, lockDuration);
    }

    @Override
    public String getTaskName() {
        return TIMER_NAME;
    }

    @Override
    public void run(KeycloakSession session) {
        findExpiredPassword();

        for (RealmModel model : realmRepository.getAllRealms()) {
            if (model.getAttribute("realmInSchedule", false)) {
                block(model.getId());
                notificationInactiveUsers(model.getId());
            }
        }
        sendEmails();
    }

    private void notificationInactiveUsers(String realm) {
        final String DEBUG_STR = "findNotifications";
        log.debug("start:{}", DEBUG_STR);
        long absenceTimeNotification = settingsService.getSettingsLongValue(SettingConstants.ABSENCE_NOTIFICATION_DAYS, realm);
        if (absenceTimeNotification > -1) {
            userHistoryLoginRepository.findInactiveUsers(absenceTimeNotification, realm);
        }
        log.debug("stop:{}", DEBUG_STR);
    }

    private void block(String realm) {
        final String DEBUG_STR = "block";
        log.debug("start:{}", DEBUG_STR);
        long absenceTimeBlock = settingsService.getSettingsLongValue(SettingConstants.ABSENCE_BLOCKING_DAYS, realm);
        if (absenceTimeBlock > -1) {
            userHistoryLoginRepository.findUsersToBlock(absenceTimeBlock, realm);
        }
        log.debug("stop:{}", DEBUG_STR);
    }

    private void findExpiredPassword() {
        final String DEBUG_STR = "findExpiredPassword";
        log.debug("start: {}", DEBUG_STR);
        List<RealmEntity> realms = policyRepository.findRealmWithPolicy(PasswordPolicy.FORCE_EXPIRED_ID);

        for (RealmEntity realm : realms) {
            try {
                String passwordPolicy = realm.getPasswordPolicy(); // forceExpiredPasswordChange(365) and passwordBlacklist(black_list_password.txt)
                if (Objects.nonNull(passwordPolicy)) {
                    //int charNumbs = PasswordPolicy.FORCE_EXPIRED_ID.length() + 3;
                    int index = passwordPolicy.indexOf(PasswordPolicy.FORCE_EXPIRED_ID);
                    if (index >= 0) {
                        int endOfPassPolicyIndex = passwordPolicy.indexOf(")", index);
                        String expirePolicy = passwordPolicy.substring(index, endOfPassPolicyIndex + 1);
                        if (expirePolicy.indexOf('(') > -1 && expirePolicy.indexOf(')') > -1) {
                            int expiresDays = Integer.parseInt(expirePolicy.substring(expirePolicy.indexOf('(') + 1, expirePolicy.lastIndexOf(')')));
                            if (expiresDays != -1) {
                                long timeToExpire = TimeUnit.DAYS.toMillis(expiresDays);
                                policyRepository.findExpiredPasswords(realm.getId(), timeToExpire);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error(DEBUG_STR, e);
            }
        }
        log.debug("stop: {}", DEBUG_STR);
    }

    private void sendEmails() {
        final String DEBUG_STR = "sendEmails";
        log.debug("start={}", DEBUG_STR);

        List<AutoLockNotification> autoLockNotifications = autoLockNotificationRepository.findNotifications();
        for (AutoLockNotification notification : autoLockNotifications) {
            UserEntity user = notification.getUser();
            RealmModel realm = realmRepository.findRealmById(user.getRealmId());
            ClientEntity client = clientRepository.findClientByIdAndRealmName(settingsService.getSettingsStringValue(SettingConstants.DEFAULT_REALM_CLIENT_ID, realm.getName()), realm.getName());
            if (client == null)
                client = clientRepository.findClientByIdAndRealmName(DEFAULT_CLIENT_ID, realm.getName());
            UserModel userModel = new UserAdapter(null, realm, null, user);
            if (notification.getType() == NotificationType.ABSENCE_NOTIFICATION) {
                long blockValue = settingsService.getSettingsLongValue(SettingConstants.BLOCK_NOTIFICATION_OF_WARNING, realm.getName());
                if (blockValue > 0) {
                    EmailModel.EmailModelBuilder prepareBlockNotification = prepareBlockNotification(userModel, realm.getName(), getClientLink(client));
                    prepareBlockNotification.realmModel(realm)
                            .user(userModel);
                    sender.send(prepareBlockNotification.build());
                }
            } else if (notification.getType() == NotificationType.ABSENCE_BLOCKING) {
                long blockValue = settingsService.getSettingsLongValue(SettingConstants.BLOCK_NOTIFICATION_OF_BLOCKED, realm.getName());
                if (blockValue > 0) {
                    EmailModel.EmailModelBuilder bockNotification = bockNotification(userModel, realm.getName());
                    bockNotification.realmModel(realm)
                            .user(userModel);
                    sender.send(bockNotification.build());
                }
                user.setEnabled(false);
                addSystemBlockAttribute(user);
                createAdminEvent(OperationType.UPDATE, user, realm);
            } else if (notification.getType() == NotificationType.PASSWORD_EXPIRED) {
                EmailModel.EmailModelBuilder passwordExpired = passwordExpired(getClientLink(client), realm.getName());
                passwordExpired.realmModel(realm)
                        .user(userModel);
                sender.send(passwordExpired.build());
            }
        }
        log.debug("stop={}", DEBUG_STR);
    }

    private void addSystemBlockAttribute(UserEntity user) {
        UserAttributeEntity attribute = new UserAttributeEntity();
        attribute.setUser(user);
        attribute.setId(UUID.randomUUID().toString());
        attribute.setName(BlockType.SYSTEM_BLOCK.getType());
        attribute.setValue(LocalDateTime.now().toString());
        userRepository.saveAttributes(attribute);
    }


    private EmailModel.EmailModelBuilder bockNotification(UserModel userModel, String realmId) {
        final String subject = "Блокирование аккаунта";
        final String template = "block-notification.ftl";
        Map<String, Object> body = new HashMap<>();
        body.put("userName", userModel.getUsername());
        List<String> phones = userModel.getAttributeStream("phone").toList();
        if (!phones.isEmpty() && phones.get(0).length() == 11) {
            String formatNumber = Util.getFormatNumber(phones.get(0));
            body.put("phone", formatNumber);
        }
        int timeTokenResetPass = settingsService.getSettingsIntValue(SettingConstants.TIME_TOKEN_RESET_PASSWORD, realmId);
        String expirationStrRusPass = Translator.getRusTranslateTimeUnitBySec(timeTokenResetPass);
        body.put("expTimePass", expirationStrRusPass);
        body.put("blockNotificationSchedulerHtml", settingsService.getSettingsStringValue(SettingConstants.SCHEDULER_BLOCKING_BODY, realmId));
        return EmailModel.builder()
                .subject(subject)
                .bodyAttributes(body)
                .bodyTemplate(template);
    }

    private EmailModel.EmailModelBuilder prepareBlockNotification(UserModel userModel, String realm, String link) {
        final String subject = "Предупреждение о блокирование аккаунта";
        final String template = "block-prepare-notification.ftl";
        SettingsDto blockSetting = settingsService.getSetting(SettingConstants.ABSENCE_BLOCKING_DAYS, realm);
        long inactiveBlockTimeout = settingsService.getSettingsLongValue(SettingConstants.ABSENCE_BLOCKING_DAYS, realm);
        long inactiveNotificationTimeout = settingsService.getSettingsLongValue(SettingConstants.ABSENCE_NOTIFICATION_DAYS, realm);
        String timeToBlock = String.valueOf(
                blockSetting.getUnit().convert(inactiveBlockTimeout - inactiveNotificationTimeout, TimeUnit.SECONDS));
        Map<String, Object> body = new HashMap<>();
        String valueTime = timeToBlock + " " + Translator.getRusTranslateTimeUnit(timeToBlock, blockSetting.getUnit());
        String bodyHtml = String.format(settingsService.getSettingsStringValue(SettingConstants.SCHEDULER_BLOCKING_PREPARE_BODY, realm), valueTime, link);
        body.put("blockPrepareNotificationSchedulerHtml", bodyHtml);
        body.put("absence", valueTime);
        body.put("link", link);
        body.put("userName", userModel.getUsername());

        int timeTokenResetPass = settingsService.getSettingsIntValue(SettingConstants.TIME_TOKEN_RESET_PASSWORD, realm);
        String expirationStrRusPass = Translator.getRusTranslateTimeUnitBySec(timeTokenResetPass);
        body.put("expTimePass", expirationStrRusPass);

        List<String> phones = userModel.getAttributeStream("phone").toList();
        if (!phones.isEmpty() && phones.get(0).length() == 11) {
            String formatNumber = Util.getFormatNumber(phones.get(0));
            body.put("phone", formatNumber);
        }
        return EmailModel.builder()
                .bodyAttributes(body)
                .subject(subject)
                .bodyTemplate(template);
    }

    private EmailModel.EmailModelBuilder passwordExpired(String link, String realmId) {
        final String subject = "Истек срок жизни пароля";
        final String template = "password-expires.ftl";

        Map<String, Object> body = new HashMap<>();
        body.put("link", link);
        String bodyHtml = String.format(settingsService.getSettingsStringValue(SettingConstants.SCHEDULER_PASSWORD_EXPIRES_BODY, realmId), link);
        body.put("passwordExpiresSchedulerHtml", bodyHtml);
        return EmailModel.builder()
                .bodyAttributes(body)
                .subject(subject)
                .bodyTemplate(template);
    }

    private String getClientLink(ClientEntity client) {

        String uri = clientService.findMainRedirectUri(client);

        if (uri != null)
            return uri;

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
            log.warn(e.getMessage(), e);
        }
        adminEvent.setResourceType("USER");
        adminEventRepository.save(adminEvent);
    }
}
