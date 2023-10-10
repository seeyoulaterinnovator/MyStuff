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
  type: ClusterIP
  loadBalancerIP: ""
  httpNodePort: ""
  # The http Service port
  httpPort: 80
  extraPorts: []

ingress:
- annotations:
    nginx.ingress.kubernetes.io/auth-keepalive-timeout: "65"
    nginx.ingress.kubernetes.io/proxy-buffering: "on"
    nginx.ingress.kubernetes.io/proxy-buffer-size: "256k"
    nginx.ingress.kubernetes.io/proxy-buffers-number: "4"
    nginx.ingress.kubernetes.io/proxy-body-size: "10m"
    nginx.ingress.kubernetes.io/proxy-cookie-path: ~^/auth/realms/(master|user)/ "/auth/realms/$1/; SameSite=None"
    nginx.ingress.kubernetes.io/configuration-snippet: >-
      set $cors '';

      if ($http_origin ~
      '^http[s]*?://(moscow\.web-23439-1\.site-frontend\.b2bweb\.t2\.ertelecom\.ru|.+\.b2bweb\.t2\.ertelecom\.ru)')
      {
          set $cors T;
      }      

      if ($request_method = 'OPTIONS') {
          set $cors "${cors}O";
      }

      if ($cors = 'T') {
          add_header 'Access-Control-Allow-Origin' "$http_origin" always;
          add_header 'Access-Control-Allow-Credentials' 'true' always;
          add_header 'Access-Control-Allow-Methods' 'GET, POST, PUT, DELETE, OPTIONS' always;
          add_header 'Access-Control-Allow-Headers' 'Accept,Authorization,Cache-Control,Content-Type,DNT,If-Modified-Since,Keep-Alive,Origin,User-Agent,X-Requested-With' always;
          add_header 'Access-Control-Allow-Headers' 'DNT,X-Mx-ReqToken,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type';

          #add_header 'Access-Control-Expose-Headers' 'Authorization' always;
      }

      if ($cors = 'O') {
          add_header 'Access-Control-Max-Age' 1728000;
          add_header 'Content-Type' 'text/plain charset=UTF-8';
          add_header 'Content-Length' 0;
          return 204;
      }

      if ($cors = 'TO') {
          add_header 'Access-Control-Allow-Origin' "$http_origin" always;
          add_header 'Access-Control-Allow-Credentials' 'true' always;
          add_header 'Access-Control-Allow-Methods' 'GET, POST, PUT, DELETE, OPTIONS' always;
          add_header 'Access-Control-Allow-Headers' 'Accept,Authorization,Cache-Control,Content-Type,DNT,If-Modified-Since,Keep-Alive,Origin,User-Agent,X-Requested-With' always;
          add_header 'Access-Control-Allow-Headers' 'DNT,X-Mx-ReqToken,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type';

          add_header 'Access-Control-Max-Age' 1728000;
          add_header 'Content-Type' 'text/plain charset=UTF-8';
          add_header 'Content-Length' 0;
          return 204;
      }
  hosts:
    - host: {{ env "FQDN" }}
      paths: /
  tls:
    - secretName: {{ env "TLS_SECRET" }}
      hosts:
        - {{ env "FQDN" }}
