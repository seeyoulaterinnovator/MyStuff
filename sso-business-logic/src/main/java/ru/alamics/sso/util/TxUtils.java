package ru.alamics.sso.util;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.KeycloakSession;

import java.util.function.Supplier;

@Slf4j
public class TxUtils {

    private TxUtils() {
    }

    /**
     * Выполняет блок кода в отдельной транзакции Keycloak.
     * Если во время выполнения возникает ошибка - откат транзакции и возрат null или проброс исключения.
     */
    public static <T> T runInTransaction(KeycloakSession session, Supplier<T> action) {
        session.getTransactionManager().begin();
        try {
            T result = action.get();
            session.getTransactionManager().commit();
            return result;
        } catch (Exception e) {
            try {
                session.getTransactionManager().rollback();
            } catch (Exception rollbackException) {
                log.error("[TxUtils] rollback failed: {}", rollbackException.getMessage(), rollbackException);
            }
            log.error("[TxUtils] transaction failed: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Выполняет блок кода в отдельной транзакции Keycloak.
     */
    public static void runInTransaction(KeycloakSession session, Runnable action) {
        session.getTransactionManager().begin();
        try {
            action.run();
            session.getTransactionManager().commit();
        } catch (Exception e) {
            try {
                session.getTransactionManager().rollback();
            } catch (Exception rollbackException) {
                log.error("[TxUtils] rollback failed: {}", rollbackException.getMessage(), rollbackException);
            }
            log.error("[TxUtils] transaction failed: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
