package ru.alamics.sso.keycloak.access.entity.provider;

import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;
import ru.alamics.sso.keycloak.access.entity.Access;

import java.util.Collections;
import java.util.List;

public class AccessJpaEntityProvider implements JpaEntityProvider {
    private static final String CHANGE_LOG = "db/changelog/db.changelog-master.xml";

    private static final String ID = "accessJpaEntityProvider";

    @Override
    public List<Class<?>> getEntities() {
        return Collections.singletonList(Access.class);
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

