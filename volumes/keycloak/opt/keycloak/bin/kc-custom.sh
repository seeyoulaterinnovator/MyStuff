#!/usr/bin/env bash
if [[ $KC_ADMIN_VITE_ENABLED == 'true' ]]
then
  export KC_ADMIN_VITE_URL="http://localhost:5174"
fi
/opt/keycloak/bin/kc.sh "$@"
