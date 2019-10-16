FROM registry.alamics.ru/devops/keycloak/keycloak:6.0.1

USER root

COPY standalone.xml /opt/jboss/keycloak/standalone/configuration
COPY standalone/deployments/ /opt/jboss/keycloak/standalone/deployments
COPY themes/ /opt/jboss/keycloak/themes
COPY standalone/data/password-blacklists /opt/jboss/keycloak/standalone/data/password-blacklists

USER 1000
