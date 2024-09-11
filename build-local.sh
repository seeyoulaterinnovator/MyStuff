#!/usr/bin/env bash
baseDir="$(cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P)"
npmRegistry=https://registry.npmjs.org

set -x
set -e

cd $baseDir/frontend/domru
npm install --registry=$npmRegistry
npm run build

cd $baseDir/frontend/ertelecom
npm install --registry=$npmRegistry
npm run build

cd $baseDir/frontend/stelecom
npm install --registry=$npmRegistry
npm run build

npm install -g pnpm

cd $baseDir/frontend-admin
pnpm install --registry=$npmRegistry
cd $baseDir/frontend-admin/js/apps/admin-ui
pnpm run build

cd $baseDir
./gradlew clean
./gradlew build
