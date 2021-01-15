FROM registry.alamics.ru/devops/keycloak/keycloak:6.0.1

USER root

COPY configs/standalone.xml /opt/jboss/keycloak/standalone/configuration
RUN mkdir -p /opt/jboss/keycloak/themes
COPY build/deploy/themes /opt/jboss/keycloak/themes
RUN mkdir -p /opt/jboss/keycloak/standalone/data/password-blacklists
COPY build/deploy/standalone/data/password-blacklists /opt/jboss/keycloak/standalone/data/password-blacklists
RUN mkdir -p /opt/jboss/keycloak/modules/system/layers/base/ru/alamics/sso/jpa/main
COPY tools/module.xml /opt/jboss/keycloak/modules/system/layers/base/ru/alamics/sso/jpa/main
COPY build/deploy/standalone/deployments/custom-jpa.jar /opt/jboss/keycloak/modules/system/layers/base/ru/alamics/sso/jpa/main
COPY build/deploy/standalone/deployments/domru-sso.war /opt/jboss/keycloak/standalone/deployments
COPY build/deploy/standalone/data/password-blacklists /opt/jboss/keycloak/standalone/data
COPY tools/modules/x1 /opt/jboss/keycloak/modules/system/layers/base
RUN cp /opt/jboss/configs/ertk.pem /etc/pki/ca-trust/source/anchors
RUN update-ca-trust

USER 1000
