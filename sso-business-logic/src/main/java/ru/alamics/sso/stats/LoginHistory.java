package ru.alamics.sso.stats;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.jpa.entity.BrandEntity;
import ru.alamics.sso.jpa.entity.UserLoginHistory;
import ru.alamics.sso.jpa.repository.BrandRepository;
import ru.alamics.sso.jpa.repository.UserHistoryLoginRepository;

import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;

@ApplicationScoped
@Slf4j
public class LoginHistory {

    @Inject
    UserHistoryLoginRepository repository;

    @Inject
    KeycloakSession session;

    @Inject
    BrandRepository brandRepository;

    /**
     * Определяем ID бренда: сначала из атрибута пользователя, если пусто — дефолтный для realm.
     */
    private String resolveBrandId(UserModel user, String realmName) {
        if (user == null || realmName == null) {
            log.warn("resolveBrandId: user or realmName is null (user={}, realmName={})",
                    user != null ? user.getId() : null, realmName);
            return null;
        }

        String brandId = user.getFirstAttribute("markBrandId");
        if (brandId != null) {
            log.debug("resolveBrandId: userId={} markBrandId={}", user.getId(), brandId);
            return brandId;
        }

        return brandRepository.findDefaultByRealm(realmName)
                .map(def -> {
                    log.info("resolveBrandId: fallback DEFAULT brand for realm {} => {} ({})",
                            realmName, def.getId(), def.getName());
                    return def.getId();
                })
                .orElseGet(() -> {
                    log.warn("resolveBrandId: DEFAULT brand not found for realm {}", realmName);
                    return null;
                });
    }

    /**
     * Определяем имя бренда по ID; если ID нет — имя дефолтного бренда для realm.
     */
    private String resolveBrandName(String brandId, String realm) {
        if (brandId == null || brandId.isBlank()) {
            log.info("resolveBrandName: brandId is null/blank for realm {}, trying default", realm);
            return brandRepository.findDefaultByRealm(realm)
                    .map(BrandEntity::getName)
                    .orElseGet(() -> {
                        log.warn("resolveBrandName: no DEFAULT brand found for realm {}", realm);
                        return null;
                    });
        }

        return brandRepository.findById(brandId)
                .map(BrandEntity::getName)
                .orElseGet(() -> {
                    log.warn("resolveBrandName: brandId {} not found in DB, falling back to DEFAULT for realm {}", brandId, realm);
                    return brandRepository.findDefaultByRealm(realm)
                            .map(BrandEntity::getName)
                            .orElse(null);
                });
    }

    public void create(UserModel user, String realmName) {
        UserLoginHistory history = buildHistory(user, realmName, false);
        if (history != null) repository.save(history);
    }

    public void createSuccessAuth(UserModel user, String realmName) {
        UserLoginHistory history = buildHistory(user, realmName, true);
        if (history != null) repository.saveSuccessAuth(history);
    }

    private UserLoginHistory buildHistory(UserModel user, String realmName, boolean success) {
        if (user == null) {
            log.warn("LoginHistory: user is null, realmName={} -> skip", realmName);
            return null;
        }

        // Fallback для realmName: берём из контекста, если не передали
        if (realmName == null) {
            RealmModel ctxRealm = session.getContext() != null ? session.getContext().getRealm() : null;
            realmName = (ctxRealm != null) ? ctxRealm.getName() : null;
        }
        if (realmName == null) {
            log.warn("LoginHistory: realmName is null for userId={} -> skip", user.getId());
            return null;
        }

        String brandId = resolveBrandId(user, realmName);
        String brandName = resolveBrandName(brandId, realmName);
        EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        UserEntity userRef = em.getReference(UserEntity.class, user.getId());

        log.debug("LoginHistory: userId={}, realmName={}, brandId={}, brandName={}, success={}",
                user.getId(), realmName, brandId, brandName, success);

        return UserLoginHistory.builder()
                .loginedAt(LocalDateTime.now())
                .realm(realmName)
                .user(userRef)
                .brandId(brandId)
                .brandName(brandName)
                .isSuccess(success)
                .build();
    }
}