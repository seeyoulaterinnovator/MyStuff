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

extraEnvs:
  JAVA_OPTS_APPEND: "-Djgroups.dns.query={{ env "CI_ENVIRONMENT_SLUG" }}-sso-headless"
  KC_CACHE: "ispn"
  KC_CACHE_CONFIG_FILE: "{{ envOrDefault  "KC_CACHE_CONFIG_FILE" "cache-ispn.embedded.xml" }}"
  KC_CACHE_STACK: "kubernetes"
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
  KC_HOSTNAME_STRICT: "true"
  KC_HOSTNAME_STRICT_HTTPS: "true"
  KC_HOSTNAME_URL: "{{ env "CI_ENVIRONMENT_URL" }}"
  # KC_LOG_CONSOLE_OUTPUT: "json" # need configure in OpenSearch
  KC_LOG_LEVEL: "INFO"
  KC_HEALTH_ENABLED: "true"
  KC_METRICS_ENABLED: "true"
  DB_DATABASE: "{{ env "DB_DATABASE" }}"
  KEYCLOAK_HOSTNAME: "{{ env "FQDN" }}"
  DB_HOST: "{{ env "DB_HOST" }}"
  DB_PORT: "{{ env "DB_PORT" }}"
  SITE: "{{ env "CI_ENVIRONMENT_SLUG" }}"
  TZ: "Asia/Yekaterinburg"
  APP_PROPS_UPDATE_DELAY_SECS: "60"
  QUARKUS_HTTP_ACCESS_LOG_ENABLED: "false"
  QUARKUS_HTTP_ACCESS_LOG_PATTERN: "long"
  DEV_HTTP_LOG_ALLOWED: "true"
  QUARKUS_LOG_CATEGORY__ORG_APACHE_HTTP__LEVEL: "DEBUG"

extraSensitiveEnvs:
  DB_USER: "{{ env "DB_USER" }}"
  DB_PASSWORD: "{{ env "DB_PASSWORD" }}"
  KC_DB_USERNAME: "{{ env "DB_USER" }}"
  KC_DB_PASSWORD: "{{ env "DB_PASSWORD" }}"

service:
  type: NodePort
  loadBalancerIP: ""
  httpNodePort: "{{ env "NODE_PORT" }}"
  # The http Service port
  httpPort: 80
  extraPorts: []

externalInfinispan:
  server: "{{ env "INFINISPAN_HOST" }}"
  port: "{{ env "INFINISPAN_PORT" }}"

resources:
  limits:
    cpu: '4'
    memory: 3Gi
  requests:
    cpu: 500m
    memory: 500Mi

