package ru.alamics.sso.keycloak.event.listener.factory.impl;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.jpa.AdminEventEntity;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.UserModel;
import ru.alamics.sso.keycloak.event.listener.factory.SsoEvent;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.model.UserEntityRepresentation;
import ru.alamics.sso.schedule.Translator;
import ru.alamics.sso.settings.SettingConstants;
import ru.alamics.sso.settings.SettingsService;
import ru.alamics.sso.stats.LoginHistory;
import ru.alamics.sso.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ru.alamics.sso.settings.SettingConstants.*;

@Slf4j
public class SsoUserUpdateEvent extends SsoEvent {

    private static final String BODY_TEMPLATE_DISABLE = "mail-disabled-account.ftl";
    private static final String BODY_TEMPLATE_ENABLE = "mail-enabled-account.ftl";

    private final AdminEvent event;

    private final SettingsService settingsService;

    @Inject
    KeycloakSession session;

    SsoUserUpdateEvent(AdminEvent event, KeycloakSession session) {
        super(session);
        settingsService = Lookup.lookup(SettingsService.class);
        this.event = event;
    }

    @Override
    public void execute() {
        try {
            KeycloakSession session = this.getSession();
            RealmProvider model = session.realms();
            UserEntityRepresentation userNow = this.getUserEntityRepresentation(event.getRepresentation());

            String userId = userNow.getId();
            if (userId == null) {
                return;
            }

            AdminEventEntity adminEventEntity = findAdminEvent(userId);
            if (userNow.isEnabled() && adminEventEntity == null) {
                return;
            }

            UserEntityRepresentation userLast = null;
            if (adminEventEntity != null) {
                userLast = getUserEntityRepresentation(adminEventEntity.getRepresentation());
            }

            if (userLast != null && userNow.isEnabled() == userLast.isEnabled()) {
                return;
            }

            log.info("ExtendedEventListener: admin update user");

            RealmModel realm = model.getRealm(this.event.getRealmId());
            UserModel user = session.users().getUserById(realm, userId);

            if (user == null || user.getEmail() == null) {
                log.error(String.format("User '%s' not found or do not have email", userId));
                return;
            }

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("userName", user.getUsername());
            attributes.put("userFirstName", user.getFirstName());
            attributes.put("userLastName", user.getLastName());

            String phone = user.getFirstAttribute("phone");
            if (phone != null && phone.length() == 11) {
                attributes.put("phone", Util.getFormatNumber(phone));
            }

            attributes.put("emailEnabledAccountBodyHtml", settingsService.getSettingsStringValue(EMAIL_ENABLE_ACCOUNT, realm.getName()));
            attributes.put("emailDisabledAccountBodyHtml", settingsService.getSettingsStringValue(EMAIL_DISABLE_ACCOUNT, realm.getName()));
            attributes.put("emailLoginAndPhoneHtml", settingsService.getSettingsStringValue(EMAIL_LOGIN_AND_PHONE_ACCOUNT, realm.getName()));
            attributes.put("emailLoginHtml", settingsService.getSettingsStringValue(EMAIL_LOGIN_ACCOUNT, realm.getName()));
            attributes.put("emailPasswordFooterHtml", settingsService.getSettingsStringValue(EMAIL_PASSWORD_FOOTER_ACCOUNT, realm.getName()));
            attributes.put("email", user.getEmail());
            int timeTokenResetPass = settingsService.getSettingsIntValue(SettingConstants.TIME_TOKEN_RESET_PASSWORD, realm.getName());
            String expirationStrRusPass = Translator.getRusTranslateTimeUnitBySec(timeTokenResetPass);
            attributes.put("expTimePass", expirationStrRusPass);
            if (userNow.isEnabled()) {
//                long blockValue = settingsService.getSettingsLongValue(SettingConstants.BLOCK_NOTIFICATION_OF_UNLOCKING, realm.getName());
//                if (blockValue > 0) {
                    this.sendEmail(user, realm, settingsService.getSettingsStringValue(ACCOUNT_SUBJECT_ENABLE, realm.getName()), BODY_TEMPLATE_ENABLE, attributes);
//                }
                this.recordLoginUser(userId);//При разблокировании юзера, логиним его
                return;
            }
            this.sendEmail(user, realm, settingsService.getSettingsStringValue(ACCOUNT_SUBJECT_DISABLE, realm.getName()), BODY_TEMPLATE_DISABLE, attributes);
        } catch (Exception e) {
            log.error("Error ", e);
        }
    }


    private AdminEventEntity findAdminEvent(String userId) {
        EntityManager em = this.getSession().getProvider(JpaConnectionProvider.class).getEntityManager();
        List<AdminEventEntity> adminEventEntities = em.createQuery("select ae " +
                "from AdminEventEntity ae " +
                "where ae.representation like concat('%', :userId, '%') " +
                "and ae.operationType in ('CREATE', 'UPDATE') " +
                "order by ae.time DESC ", AdminEventEntity.class)
                .setParameter("userId", userId)
                .getResultList();
        if (adminEventEntities == null || adminEventEntities.isEmpty() || adminEventEntities.size() == 1) {
            return null;
        }
        return adminEventEntities.get(1);
    }


    private void recordLoginUser(final String userId) {
        LoginHistory loginHistoryService = Lookup.lookup(LoginHistory.class);
        Optional.ofNullable(loginHistoryService).ifPresent(loginHistory -> {
           RealmModel realm = session.getContext().getRealm();
           if (realm == null) {
               log.warn("recordLoginUser: realm is null, cannot record login for user {}", userId);
               return;
           }

           UserModel userModel = session.users().getUserById(realm, userId);
           if (userModel == null) {
               log.warn("recordLoginUser: user {} not found in realm {}", userId, realm.getName());
               return;
           }

           loginHistory.create(userModel, realm.getName());
        });
    }
}
