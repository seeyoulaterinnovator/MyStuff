package org.keycloak.connections.jpa.updater.liquibase;

import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.MariaDBDatabase;
import liquibase.exception.DatabaseException;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.config.database.Database;
import org.mariadb.jdbc.message.server.util.ServerVersionUtility;

import java.util.HashSet;
import java.util.Set;

/**
 * Замена UpdatedMariaDBDatabase (не переопределение, из не возможности переопределить через SPI)
 * класса провайдера БД для Liquibase,
 * т.е. полагаемся на фиксированный порядок class loader-ов у Quarkus
 * @see Database.Vendor#MARIADB
 * @see org.keycloak.connections.jpa.updater.liquibase.UpdatedMySqlDatabase
 */
@Slf4j
public class UpdatedMariaDBDatabase extends MariaDBDatabase {
    //#region Взято из оригинального переопределения (нужно повторно скопировать при обновлении версии)
    private static final Set<String> RESERVED_WORDS = new HashSet<>();

    public UpdatedMariaDBDatabase() {
        super();
        log.warn("UpdatedMariaDBDatabase replaced");
    }

    @Override
    public boolean isReservedWord(String string) {
        return super.isReservedWord(string) || RESERVED_WORDS.contains(string.toUpperCase());
    }

    @Override
    public int getPriority() {
        return super.getPriority() + 1; // Always take precedence over factory MariaDBDatabase
    }

    static {
        RESERVED_WORDS.add("PERIOD");
    }
    //#endregion

    /**
     * То ради чего произведена замена.
     * Переопределение метода проверки совместимости соединения,
     * чтобы преодолеть ошибку "Unknown database: MySQL"
     * ({@link DatabaseFactory#findCorrectDatabaseImplementation(DatabaseConnection)}),
     * причиной которой является некорректное значение "MySQL" вместо "MariaDB"
     * для {@link java.sql.DatabaseMetaData#getDatabaseProductName} от драйвера,
     * определяемое на основе 1-ых пакетов при соединении с СУБД,
     * см. {@link ServerVersionUtility#isMariaDBServer()}
     * и {@link org.mariadb.jdbc.message.server.InitialHandshakePacket}.
     * Ошибка наблюдается только для случая обращения через ProxySQL в Galera Cluster
     */
    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return super.isCorrectDatabaseImplementation(conn) ||
                "MYSQL".equalsIgnoreCase(conn.getDatabaseProductName())
                && "MARIADB".equalsIgnoreCase(System.getenv("KC_DB"));
    }
}
