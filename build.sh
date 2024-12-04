#!/usr/bin/env bash
baseDir="$(cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P)"
set -x
set -e

npm install -g pnpm

cd $baseDir/frontend/domru
npm install
npm run build

cd $baseDir/frontend/ertelecom
npm install
npm run build

cd $baseDir/frontend/stelecom
npm install
npm run build

cd $baseDir/frontend/novotelecom
npm install
npm run build

cd $baseDir/frontend-admin
pnpm install
cd $baseDir/frontend-admin/js/apps/admin-ui
pnpm run build

cd $baseDir
./gradlew clean
./gradlew build
