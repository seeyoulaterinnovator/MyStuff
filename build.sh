#!/usr/bin/env bash
baseDir="$(cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P)"

NPM_REGISTRY=https://registry.npmjs.org
if [[ -z "${KC_ADMIN_VITE_ENABLED}" ]]
then
  export NPM_REGISTRY="https://registry.npmjs.org"
fi

npm install --registry=$NPM_REGISTRY -g pnpm

set -x
set -e

cd $baseDir/frontend/domru
npm install --registry=$NPM_REGISTRY
npm run build

cd $baseDir/frontend/ertelecom
npm install --registry=$NPM_REGISTRY
npm run build

cd $baseDir/frontend/stelecom
npm install --registry=$NPM_REGISTRY
npm run build

cd $baseDir/frontend-admin
pnpm install --registry=$NPM_REGISTRY
cd $baseDir/frontend-admin/js/apps/admin-ui
pnpm run build

cd $baseDir
./gradlew clean
./gradlew build
