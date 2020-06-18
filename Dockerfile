FROM registry.alamics.ru/devops/keycloak/keycloak:6.0.1

USER root

COPY standalone.xml /opt/jboss/keycloak/standalone/configuration
COPY standalone/deployments/ /opt/jboss/keycloak/standalone/deployments
COPY themes/ /opt/jboss/keycloak/themes
COPY standalone/data/password-blacklists /opt/jboss/keycloak/standalone/data/password-blacklists
RUN mkdir -p /opt/jboss/keycloak/modules/system/layers/base/ru/alamics/sso/jpa/main
COPY tools/module.xml /opt/jboss/keycloak/modules/system/layers/base/ru/alamics/sso/jpa/main
RUN pwd
RUN ls -l
RUN ls -l build
RUN ls -l build
RUN ls -l build/custom-jpa-provider
RUN ls -l build/custom-jpa-provider/libs
COPY build/custom-jpa-provider/libs/custom-jpa-provider-0.0.1.jar /opt/jboss/keycloak/modules/system/layers/base/ru/alamics/sso/jpa/main/custom-jpa.jar
RUN cp /opt/jboss/configs/ertk.pem /etc/pki/ca-trust/source/anchors
RUN update-ca-trust

USER 1000
