FROM jboss/keycloak

COPY domru-sso.war /opt/jboss/keycloak/standalone/deployments
