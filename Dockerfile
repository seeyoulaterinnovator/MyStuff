FROM jboss/base-jdk:11

ENV KEYCLOAK_VERSION 6.0.1
ENV JDBC_POSTGRES_VERSION 42.2.5
ENV JDBC_MYSQL_VERSION 5.1.46
ENV JDBC_MARIADB_VERSION 2.2.3

ENV LAUNCH_JBOSS_IN_BACKGROUND 1
ENV PROXY_ADDRESS_FORWARDING false
ENV JBOSS_HOME /opt/jboss/keycloak
ENV LANG en_US.UTF-8

ARG GIT_REPO
ARG GIT_BRANCH
ARG KEYCLOAK_DIST=https://downloads.jboss.org/keycloak/$KEYCLOAK_VERSION/keycloak-$KEYCLOAK_VERSION.tar.gz

USER root

RUN yum update -y && yum install -y epel-release git && yum install -y jq openssl which && yum clean all

#
# Remove me after debugging
#

RUN yum install -y tcpdump telnet bind-tools iproute

ADD tools /opt/jboss/tools
RUN /opt/jboss/tools/build-keycloak.sh

COPY standalone.xml /opt/jboss/keycloak/standalone/configuration
COPY standalone/deployments/ /opt/jboss/keycloak/standalone/deployments
COPY themes/ /opt/jboss/keycloak/themes

USER 1000

EXPOSE 8080
EXPOSE 8443

ENTRYPOINT [ "/opt/jboss/tools/docker-entrypoint.sh" ]

CMD ["-b", "0.0.0.0"]
