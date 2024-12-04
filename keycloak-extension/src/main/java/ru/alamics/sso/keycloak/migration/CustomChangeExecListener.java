package ru.alamics.sso.keycloak.migration;

import liquibase.change.custom.CustomChangeWrapper;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.visitor.DefaultChangeExecListener;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomChangeExecListener extends DefaultChangeExecListener {
    @Override
    public void willRun(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, ChangeSet.RunStatus runStatus) {
        if("META-INF/jpa-changelog-13.0.0.xml::default-roles::keycloak".contains(getChangeSetId(changeSet))) {
            changeSet.getChanges().stream()
                    .filter(CustomChangeWrapper.class::isInstance)
                    .map(CustomChangeWrapper.class::cast)
                    .filter(change -> change.getClassName().endsWith(JpaUpdate13_0_0_MigrateDefaultRoles.class.getSimpleName()))
                    .forEach(change -> {
                        try {
                            change.setClass(JpaUpdate13_0_0_MigrateDefaultRoles.class.getCanonicalName());
                        } catch (CustomChangeException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    @Override
    public void runFailed(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Exception exception) {
        log.error(exception.getMessage(), exception);
    }

    private String getChangeSetId(ChangeSet changeSet) {
        return changeSet.getFilePath() + "::" + changeSet.getId() + "::" + changeSet.getAuthor();
    }

}
