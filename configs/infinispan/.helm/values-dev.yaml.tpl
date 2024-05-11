image:
  repository: jboss/infinispan-server
  tag: 9.4.8.Final
  pullPolicy: IfNotPresent

imagePullSecrets: []

serviceAccount: ""

podSecurityContext: {}

replicaCount: "{{ env "INFINISPAN_REPLICA_COUNT" }}"

site: "staging"

credentials:
  user: "{{ env "INFINISPAN_USER" }}"
  password: "{{ env "INFINISPAN_PASSWORD" }}"

extraEnvs:
  JAVA_OPTS: '-server -verbose:gc -Xloggc:"/opt/jboss/infinispan-server/standalone/log/gc.log" -XX:+PrintGCDetails -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=5 -XX:GCLogFileSize=3M -XX:-TraceClassUnloading -Xms128m -Xmx768m -XX:MetaspaceSize=96M -XX:MaxMetaspaceSize=256m -Djava.net.preferIPv4Stack=true -Djboss.modules.system.pkgs=org.jboss.byteman -Djava.awt.headless=true'

managementService:
  type: NodePort
  loadBalancerIP: ""
  managementNodePort: "30990"
  restNodePort: "30991"
  # The http Service port
  managementPort: 80
  restPort: 81
  extraPorts: []

resources:
  requests:
    cpu: "0.5"
    memory: 512Mi
  limits:
    cpu: "1"
    memory: 1Gi

podAnnotations:
  dtdigest: "{{ env "DTD" }}"
