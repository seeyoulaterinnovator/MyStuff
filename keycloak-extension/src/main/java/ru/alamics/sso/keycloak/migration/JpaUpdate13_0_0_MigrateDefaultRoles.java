package ru.alamics.sso.keycloak.migration;

import liquibase.exception.CustomChangeException;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Copied class with fix of error "duplicate key value violates unique constraint "UK_J3RWUVD56ONTGSUHOGM184WW2-2"
 *
 * @link <a href="https://github.com/keycloak/keycloak/issues/23220">Issue</a>
 * @link <a href="https://github.com/keycloak/keycloak/pull/23560">MR</a>
 */
@Slf4j
public class JpaUpdate13_0_0_MigrateDefaultRoles extends org.keycloak.connections.jpa.updater.liquibase.custom.JpaUpdate13_0_0_MigrateDefaultRoles {
    private static final AtomicBoolean IS_EXECUTED = new AtomicBoolean(false);

    @Override
    protected void generateStatementsImpl() throws CustomChangeException {
        if (IS_EXECUTED.get()) {
            log.warn("Task \"{}\" is already executed", getTaskId());
            return;
        }
        IS_EXECUTED.set(true);
        log.info("Task \"{}\" is running", getTaskId());
        super.generateStatementsImpl();
    }
}
