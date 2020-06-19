FROM registry.alamics.ru/devops/keycloak/keycloak:6.0.1

USER root

COPY configs/standalone.xml /opt/jboss/keycloak/standalone/configuration
COPY build/deploy/themes /opt/jboss/keycloak
COPY build/deploy/standalone/data/password-blacklists /opt/jboss/keycloak/standalone/data
RUN mkdir -p /opt/jboss/keycloak/modules/system/layers/base/ru/alamics/sso/jpa/main
COPY tools/module.xml /opt/jboss/keycloak/modules/system/layers/base/ru/alamics/sso/jpa/main
COPY build/deploy/standalone/deployments/custom-jpa.jar /opt/jboss/keycloak/modules/system/layers/base/ru/alamics/sso/jpa/main
COPY build/deploy/standalone/deployments/domru-sso.war /opt/jboss/keycloak/standalone/deployments
RUN cp /opt/jboss/configs/ertk.pem /etc/pki/ca-trust/source/anchors
RUN update-ca-trust

USER 1000
