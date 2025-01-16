#!/usr/bin/env bash
./bin/launch.sh -c /user-config/cache-ispn-xsite.xml \
  -Dinfinispan.site.name=$SITE \
  -Djgroups.mcast_port=$MCAST_PORT \
  -Dinfinispan.backup.site.name=$BACKUP_SITE
