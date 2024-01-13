image:
  repository: harbor.ertelecom.ru/sso-protected/sso
  tag: {{ env "CI_PIPELINE_ID" }}
  pullPolicy: Always

serviceAccount:
  create: false

imagePullSecrets:
  - name: regcred

replicaCount: 3

preStopDelay:
  enabled: true
  delaySeconds: 15

extraEnvs:
  DB_DATABASE: "{{ env "DB_DATABASE" }}"
  KEYCLOAK_HOSTNAME: "{{ env "FQDN" }}"
  DB_HOST: "{{ env "DB_HOST" }}"
  DB_PORT: "{{ env "DB_PORT" }}"
  SITE: "stage"
  TZ: "Asia/Yekaterinburg"  

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
