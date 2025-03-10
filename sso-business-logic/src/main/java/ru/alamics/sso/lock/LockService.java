package ru.alamics.sso.lock;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.jpa.entity.LockEntity;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.status.StatusService;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

/**
 * Сервис блокировки выполнения задач между процессами
 * (через БД, т.к. не будет общего Infinispan кэша в cross DC варианте)
 */
@ApplicationScoped
@Slf4j
public class LockService {
    private static final String EMPTY_OWNER = "none";

    final RetryPolicy<Object> retryPolicy = RetryPolicy.builder()
            .handle(Exception.class)
            .withDelay(Duration.ofMillis(100))
            .withJitter(0.5)
            .withMaxRetries(10)
            .build();

    @Inject
    EntityManager em;

    @Inject
    ApplicationProperties properties;

    @Inject
    StatusService statusService;

    String owner;

    @PostConstruct
    protected void initialize() {
        owner = statusService.getNodeName();
    }

    public boolean tryLock(@NonNull String id, Duration expire) {
        if(isEnabled()) {
            try {
                return Failsafe.with(retryPolicy).get(() -> attemptLock(id, owner, expire));
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
        }
        return false;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    protected boolean attemptLock(@NonNull String id, @NonNull String owner, Duration expire) {
        if (owner.equals(EMPTY_OWNER)) throw new IllegalArgumentException(owner);

        @Cleanup Stream<LockEntity> locks = em.createQuery(
                        "select e from LockEntity e where id = :id",
                        LockEntity.class
                )
                .setParameter("id", id)
                .setLockMode(LockModeType.OPTIMISTIC)
                .getResultStream();
        LockEntity lock = locks.findFirst().orElse(null);
        Instant now = Instant.now();
        Instant nextAcquireDate = now.plus(expire);
        if (lock == null) {
            lock = new LockEntity();
            lock.setId(id);
            lock.setOwner(owner);
            lock.setAcquireDate(nextAcquireDate);
            em.merge(lock);
        } else {
            if (lock.getOwner().equals(owner)) {
                if (Duration.between(now, lock.getAcquireDate())
                        .minus(expire.dividedBy(2))
                        .isNegative()) {
                    lock.setAcquireDate(nextAcquireDate);
                }
            } else if (lock.getAcquireDate().isBefore(now) || lock.getOwner().equals(EMPTY_OWNER)) {
                lock.setOwner(owner);
                lock.setAcquireDate(nextAcquireDate);
            } else {
                return false;
            }
        }
        em.flush();
        return true;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void unlock(@NonNull String id) {
        if(isEnabled()) {
            em.createQuery("update LockEntity set owner = :emptyOwner where id = :id and owner = :owner")
                    .setParameter("id", id)
                    .setParameter("emptyOwner", EMPTY_OWNER)
                    .setParameter("owner", owner)
                    .executeUpdate();
            em.flush();
        }
    }

    private boolean isEnabled() {
        return !properties.getProperty("db.mutex.disabled", "false").equals("true");
    }
}
