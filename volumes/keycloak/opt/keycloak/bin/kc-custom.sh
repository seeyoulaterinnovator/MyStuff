#!/usr/bin/env bash
if [[ $KC_ADMIN_VITE_ENABLED == 'true' ]]
then
  export KC_ADMIN_VITE_URL="http://localhost:5174"
  export KC_SPI_THEME_FOLDER_DIR=/opt/keycloak/themes-custom
fi

export JAVA_OPTS="${JAVA_OPTS} -Dcom.sun.management.jmxremote
-Dcom.sun.management.jmxremote.port=9990
-Dcom.sun.management.jmxremote.rmi.port=9990
-Djava.rmi.server.hostname=0.0.0.0
-Dcom.sun.management.jmxremote.authenticate=false
-Dcom.sun.management.jmxremote.ssl=false
-Dcom.sun.management.jmxremote.local.only=false"

/opt/keycloak/bin/kc.sh start-dev
