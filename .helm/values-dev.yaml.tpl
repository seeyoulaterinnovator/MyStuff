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
  DB_DATABASE: "{{ env "DB_DATABASE" }}"
  KEYCLOAK_HOSTNAME: "{{ env "FQDN" }}"
  DB_HOST: "{{ env "DB_HOST" }}"
  DB_PORT: "{{ env "DB_PORT" }}"
  SITE: "{{ env "CI_ENVIRONMENT_SLUG" }}"
  TZ: "Asia/Yekaterinburg"
  JAVA_OPTS: "-server -Xms64m -Xmx512m -XX:MetaspaceSize=96M -XX:MaxMetaspaceSize=256m -Djava.net.preferIPv4Stack=true -Djboss.modules.system.pkgs=org.jboss.byteman -Djboss.site.name={{ env "CI_ENVIRONMENT_SLUG" }} -Djava.awt.headless=true"

extraSensitiveEnvs:
  DB_USER: "{{ env "DB_USER" }}"
  DB_PASSWORD: "{{ env "DB_PASSWORD" }}"

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
    cpu: '2'
    memory: 1.5Gi
  requests:
    cpu: 500m
    memory: 500Mi

ingress:
  - annotations:
      # viber://* - для чата
      nginx.ingress.kubernetes.io/configuration-snippet: |
        add_header Content-Security-Policy frame-src 'self' https://* wss://* viber://*; frame-ancestors 'self' *; object-src 'none';

