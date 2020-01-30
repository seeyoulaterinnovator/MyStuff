package ru.alamics.sso.keycloak.entity.provider;

import ru.alamics.sso.keycloak.entity.*;

import java.util.List;

import static ru.alamics.sso.keycloak.entity.provider.factory.CustomJpaProviderFactory.ID;

public class CustomJpaEntityProvider implements org.keycloak.connections.jpa.entityprovider.JpaEntityProvider {
    private static final String CHANGE_LOG = "db/changelog/db.changelog-master.xml";


    @Override
    public List<Class<?>> getEntities() {
        return List.of(UserLoginHistory.class,
                UserPostRoleEntity.class,
                AutoLockNotification.class,
                UserPostEntity.class,
                ExternalSystemEntity.class,
                ExternalSystemRoleEntity.class,
                Settings.class,
                Customer.class
        );
    }

    @Override
    public String getChangelogLocation() {
        return CHANGE_LOG;
    }

    @Override
    public String getFactoryId() {
        return ID;
    }

    @Override
    public void close() {

    }
}