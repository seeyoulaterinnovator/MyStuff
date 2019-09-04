package ru.alamics.sso.keycloak.entity.provider;

import ru.alamics.sso.keycloak.entity.UserLoginHistory;

import java.util.Collections;
import java.util.List;

public class CustomJpaEntityProvider implements org.keycloak.connections.jpa.entityprovider.JpaEntityProvider {
    private static final String CHANGE_LOG = "db/changelog/db.changelog-master.xml";

    private static final String ID = "customJpaEntityProvider";

    @Override
    public List<Class<?>> getEntities () {
        return Collections.singletonList(UserLoginHistory.class);
    }

    @Override
    public String getChangelogLocation () {
        return CHANGE_LOG;
    }

    @Override
    public String getFactoryId () {
        return ID;
    }

    @Override
    public void close () {

    }
}