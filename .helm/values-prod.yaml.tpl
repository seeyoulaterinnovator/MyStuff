image:
  repository: harbor.ertelecom.ru/sso-protected/sso
  tag: {{ env "CI_PIPELINE_ID" }}
  pullPolicy: Always

serviceAccount:
  create: false

imagePullSecrets:
  - name: regcred

replicaCount: "{{ env "REPLICA_COUNT" }}"

preStopDelay:
  enabled: true
  delaySeconds: 15

monitoring:
 authMetrics: false

extraEnvs:
  KC_FEATURES: "{{ envOrDefault  "KC_FEATURES" "" }}"
  KC_CACHE: "ispn"
  KC_CACHE_CONFIG_FILE: "{{ envOrDefault  "KC_CACHE_CONFIG_FILE" "cache-ispn.remote.xml" }}"
  KC_CACHE_STACK: "{{ envOrDefault "KC_CACHE_STACK" "tcp" }}"
  {{- if eq (env "KC_CACHE_STACK") "kubernetes" }}
  JAVA_OPTS_APPEND: "-Djgroups.dns.query={{ env "CI_ENVIRONMENT_SLUG" }}-sso-headless"
  {{- else }}
  KC_CACHE_REMOTE_HOST: "{{ env "INFINISPAN_HOST" }}"
  KC_CACHE_REMOTE_PORT: "{{ envOrDefault  "INFINISPAN_PORT" "11222" }}"
  KC_CACHE_REMOTE_TLS_ENABLED: "false"
  KC_SPI_AUTHENTICATION_SESSIONS_PROVIDER: "custom-remote"
  {{- end }}
  KC_DB: "mariadb"
  KC_DB_URL_HOST: "{{ env "DB_HOST" }}"
  KC_DB_URL_PORT: "{{ env "DB_PORT" }}"
  KC_DB_URL_DATABASE: "{{ env "DB_DATABASE" }}"
  KC_HTTP_ENABLED: "true"
  KC_HTTP_RELATIVE_PATH: "/auth"
  KC_HOSTNAME: "{{ env "FQDN" }}"
  KC_HOSTNAME_ADMIN_URL: "{{ env "CI_ENVIRONMENT_URL" }}" # see https://github.com/keycloak/keycloak/issues/16005
  KC_HOSTNAME_ACCOUNT_URL: "{{ env "CI_ENVIRONMENT_URL" }}"
  KC_HOSTNAME_DEBUG: "true"
  KC_HOSTNAME_STRICT: "false"
  KC_HOSTNAME_STRICT_HTTPS: "false"
  KC_HOSTNAME_URL: "{{ env "CI_ENVIRONMENT_URL" }}"
  # KC_LOG_CONSOLE_OUTPUT: "json" # need configure in OpenSearch
  KC_LOG_LEVEL: "INFO"
  KC_HEALTH_ENABLED: "true"
  KC_METRICS_ENABLED: "true"
  DB_DATABASE: "{{ env "DB_DATABASE" }}"
  KEYCLOAK_HOSTNAME: "{{ env "FQDN" }}"
  DB_HOST: "{{ env "DB_HOST" }}"
  DB_PORT: "{{ env "DB_PORT" }}"
  SITE: "prod"
  TZ: "Asia/Yekaterinburg"
  APP_PROPS_UPDATE_DELAY_SECS: "60"
  QUARKUS_HTTP_ACCESS_LOG_ENABLED: "true" # TODO отключить
  # QUARKUS_HTTP_ACCESS_LOG_PATTERN: "long"
  DEV_HTTP_LOG_ALLOWED: "true"
  QUARKUS_LOG_CATEGORY__ORG_APACHE_HTTP__LEVEL: "DEBUG" # TODO отключить
  # fix "java.io.EOFException: unexpected end of stream, read 0 bytes from 4 (socket was closed by server)"
  KC_DB_URL_PROPERTIES: "?usePipeLineAuth=false&disablePipeLine=true&useBatchMultiSend=false"
  DC_NAME: "{{ envOrDefault "DC_NAME" "voronezh" }}"
  PROXY_ADDRESS_FORWARDING: "true"

extraSensitiveEnvs:
  DB_USER: "{{ env "DB_USER" }}"
  DB_PASSWORD: "{{ env "DB_PASSWORD" }}"
  KC_DB_USERNAME: "{{ env "DB_USER" }}"
  KC_DB_PASSWORD: "{{ env "DB_PASSWORD" }}"
  KC_CACHE_REMOTE_USERNAME: "{{ env "INFINISPAN_USER" }}"
  KC_CACHE_REMOTE_PASSWORD: "{{ env "INFINISPAN_PASSWORD" }}"

service:
  type: NodePort
  loadBalancerIP: ""
  httpNodePort: "{{ env "NODE_PORT" }}"
  # The http Service port
  httpPort: 80
  extraPorts: []

resources:
  limits:
    cpu: "4"
    memory: 8Gi
  requests:
    cpu: 500m
    memory: 500Mi

podAntiAffinity:
  requiredDuringSchedulingIgnoredDuringExecution:
  - labelSelector:
      matchExpressions:
      - key: app.kubernetes.io/instance
        operator: In
        values:
        - "{{ env "CI_ENVIRONMENT_SLUG" }}"
    topologyKey: kubernetes.io/hostname
