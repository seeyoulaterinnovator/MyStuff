package ru.alamics.sso.keycloak.event.listener.factory.impl;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.jpa.AdminEventEntity;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.keycloak.event.listener.factory.SsoEvent;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.model.UserEntityRepresentation;
import ru.alamics.sso.stats.LoginHistory;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class SsoUserUpdateEvent extends SsoEvent {

    private final AdminEvent event;

    SsoUserUpdateEvent(AdminEvent event, KeycloakSession session) {
        super(session);
        this.event = event;
    }

    @Override
    public void execute() {
        try {
            var session = this.getSession();
            var model = session.realms();
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
            if (adminEventEntity != null){
                userLast = getUserEntityRepresentation(adminEventEntity.getRepresentation());
            }

            if (userLast != null && userNow.isEnabled() == userLast.isEnabled()) {
                return;
            }

            log.info("ExtendedEventListener: admin update user");

            RealmModel realm = model.getRealm(this.event.getRealmId());
            UserModel user = session.users().getUserById(userId, realm);

            if (user == null || user.getEmail() == null) {
                log.error(String.format("User '%s' not found or do not have email", userId));
                return;
            }

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("userName", user.getUsername());
            attributes.put("userFirstName", user.getFirstName());
            attributes.put("userLastName", user.getLastName());

            if (userNow.isEnabled()) {
                this.sendEmail(user, realm, "emailEnabledAccountSubject", "mail-enabled-account.ftl", attributes);
                this.recordLoginUser(userId);//При разблокировании юзера, логиним его
                return;
            }
            this.sendEmail(user, realm, "emailDisabledAccountSubject", "mail-disabled-account.ftl", attributes);
        } catch (Exception e) {
            log.error("Error ", e);
        }
    }


    private AdminEventEntity findAdminEvent(String userId) {
        EntityManager em = this.getSession().getProvider(JpaConnectionProvider.class).getEntityManager();
        List<AdminEventEntity> adminEventEntities = em.createQuery("" +
                "select ae " +
                "from AdminEventEntity ae " +
                "where ae.representation like concat('%', :userId, '%') " +
                "order by ae.time DESC ", AdminEventEntity.class)
                .setParameter("userId", userId)
                .getResultList();
        if (adminEventEntities == null || adminEventEntities.isEmpty() || adminEventEntities.size() == 1) {
            return null;
        }
        return adminEventEntities.get(1);
    }


    private void recordLoginUser(final String userId) {
        UserEntity entity = new UserEntity();
        entity.setId(userId);

        LoginHistory loginHistoryService = (LoginHistory) Lookup.lookup(LoginHistory.class);
        Optional.ofNullable(loginHistoryService).ifPresent(loginHistory -> {
            loginHistory.create(entity);
        });

    }
}
