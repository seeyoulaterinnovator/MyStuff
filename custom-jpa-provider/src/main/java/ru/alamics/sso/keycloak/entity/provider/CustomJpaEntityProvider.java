package ru.alamics.sso.keycloak.entity.provider;

import ru.alamics.sso.keycloak.entity.UserPostRole;
import ru.alamics.sso.keycloak.entity.UserLoginHistory;
import ru.alamics.sso.keycloak.entity.UserPost;

import java.util.List;

public class CustomJpaEntityProvider implements org.keycloak.connections.jpa.entityprovider.JpaEntityProvider {
    private static final String CHANGE_LOG = "db/changelog/db.changelog-master.xml";

    private static final String ID = "customJpaEntityProvider";

    @Override
    public List<Class<?>> getEntities () {
        return List.of(UserLoginHistory.class, UserPostRole.class, System.class, UserPost.class);
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