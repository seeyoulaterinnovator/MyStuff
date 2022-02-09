package ru.alamics.sso.jpa.entity.provider;

import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;
import ru.alamics.sso.jpa.entity.*;
import ru.alamics.sso.jpa.entity.provider.factory.CustomJpaProviderFactory;

import java.util.Arrays;
import java.util.List;

public class CustomJpaEntityProvider implements JpaEntityProvider {
    private static final String CHANGE_LOG = "db/changelog/db.changelog-master.xml";

    @Override
    public List<Class<?>> getEntities() {
        return Arrays.asList(UserLoginHistory.class,
                UserPostRoleEntity.class,
                AutoLockNotification.class,
                UserPostEntity.class,
                ExternalSystemEntity.class,
                ExternalSystemRoleEntity.class,
                Customer.class,
                Settings.class,
                AppProperty.class,
                MainRedirectUri.class
        );
    }

    @Override
    public String getChangelogLocation() {
        return CHANGE_LOG;
    }

    @Override
    public String getFactoryId() {
        return CustomJpaProviderFactory.PROVIDER_ID;
    }

    @Override
    public void close() {

    }
}