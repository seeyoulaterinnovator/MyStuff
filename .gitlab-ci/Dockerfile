FROM quay.io/keycloak/keycloak:25.0.2

COPY --chown=keycloak ./volumes/keycloak/opt/keycloak/conf/cache-ispn-custom.xml /opt/keycloak/conf/
COPY --chown=keycloak ./build/keycloak-extension/libs/keycloak-extension-1.0.1-all.jar /opt/keycloak/providers/

ENV LIQUIBASE_COMMAND_CHANGE_EXEC_LISTENER_CLASS=ru.alamics.sso.keycloak.migration.CustomChangeExecListener
