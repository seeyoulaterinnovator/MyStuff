# FROM harbor.ertelecom.ru/sso-protected/keycloak:6.0.1
FROM quay.io/keycloak/keycloak:6.0.1

USER root

COPY configs/ertk.pem /etc/pki/ca-trust/source/anchors
RUN update-ca-trust
RUN chmod 755 /opt/jboss/tools/docker-entrypoint.sh

RUN mkdir -p /opt/jboss/keycloak/themes
RUN mkdir -p /opt/jboss/keycloak/standalone/data/password-blacklists
RUN mkdir -p /opt/jboss/keycloak/modules/system/layers/base/ru/alamics/sso/jpa/main

COPY configs/standalone.xml /opt/jboss/keycloak/standalone/configuration
COPY configs/standalone-ha-infspn-ext.xml /opt/jboss/keycloak/standalone/configuration
COPY configs/standalone-ha-infspn-int.xml /opt/jboss/keycloak/standalone/configuration

COPY tools/module.xml /opt/jboss/keycloak/modules/system/layers/base/ru/alamics/sso/jpa/main
COPY tools/modules /opt/jboss/keycloak/modules/system/layers/base

COPY build/deploy/themes /opt/jboss/keycloak/themes
COPY build/deploy/standalone/data/password-blacklists /opt/jboss/keycloak/standalone/data/password-blacklists
COPY build/deploy/standalone/deployments/custom-jpa.jar /opt/jboss/keycloak/modules/system/layers/base/ru/alamics/sso/jpa/main
COPY build/deploy/standalone/deployments/domru-sso.war /opt/jboss/keycloak/standalone/deployments
COPY build/deploy/standalone/data/password-blacklists /opt/jboss/keycloak/standalone/data

USER jboss
