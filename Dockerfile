FROM quay.io/keycloak/keycloak:25.0.2
COPY ./build/keycloak-extension/libs/keycloak-extension-1.0.1-all.jar /opt/keycloak/providers/
