FROM registry.alamics.ru/devops/keycloak/keycloak:6.0.1

USER root

ADD tools /opt/jboss/tools
RUN /opt/jboss/tools/build-keycloak.sh

COPY standalone.xml /opt/jboss/keycloak/standalone/configuration
COPY standalone/deployments/ /opt/jboss/keycloak/standalone/deployments
COPY themes/ /opt/jboss/keycloak/themes

USER 1000
