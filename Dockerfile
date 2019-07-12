FROM jboss/keycloak

COPY standalone.xml /opt/jboss/keycloak/standalone/configuration
COPY domru-sso.war /opt/jboss/keycloak/standalone/deployments
