# Frontend admin

## Оглавление

1. [Стенды](#Описание-и-структура-проекта)
2. [Описание и структура проекта](#Описание-и-структура-проекта)
3. [Требования](#Требования)
4. [Локальный запуск проекта](#Локальный-запуск-проекта)
5. [Интернационализация](#Интернационализация)
6. [Дополнительные источники](#Дополнительные-источники)

## Стенды 

- **prod**: [https://auth.dom.ru](https://auth.dom.ru/auth/admin/master/console/#/realms)

- **test**: [https://sso-balancer4.testing.srv.loc](https://sso-balancer4.testing.srv.loc/auth/admin/master/console/#/realms)

- **dev**: [https://er-sso.dev.it-resource.team](https://er-sso.dev.it-resource.team/auth/admin/master/console/#/realms)

### Keycloak консоль доступна в двух основных вариациях:
- master - консоль администратора (/auth/admin/master/console/#/realms)
- manager - консоль менеджера (/auth/admin/manager/console/#/realms)

## Описание и структура проекта 

Конфигурирование темы административной консоли основывается на разработке [React приложения](./frontend-admin/js/apps/admin-ui). Подробнее с кастомизацией тем, основанных на React, можно ознакомиться в [документации](https://www.keycloak.org/docs/latest/server_development/#_theme_react). 
⚠️ _На момент разработки собственной темы административной консоли пакета *@keycloak/keycloak-admin-ui* не существовало, хотя он и был описан в документации. В связи с этим было найдено [альтернативное решение](https://github.com/keycloak/keycloak/discussions/32645#discussioncomment-10566232)._

Каталог [frontend-admin]('../frontend-admin') является копией каталога [keycloak/js](https://github.com/keycloak/keycloak/tree/main/js).

С описанием исходной структурой проекта можно ознакомиться в [данном месте](https://github.com/keycloak/keycloak/blob/main/js/README.md).

### Cтруктура проекта 

    ├── apps
    │   ├── admin-ui                   # Admin UI for handling login, registration, administration, and account management
    ├── libs
    │   ├── keycloak-admin-client      # Keycloak Admin Client library for Keycloak REST API
    │   └── keycloak-js                # Keycloak JS library for securing HTML5/JavaScript applications
    ├── ...

## Требования

- **Node** v20 (например, 20.5.0)
- **npm** v9 (например, 9.8.0)

## Локальный запуск проекта

1. Запустить сервер в соответствии с [инструкцией Разработка](../README.MD/#Разработка)
2. Проинициализировать husky: `pnpm prepare`;
3. Установить зависимости: `npm i`
4. Запустить проект из каталога `/js/apps/admin-ui`: `pnpm dev`
5. Открыть [localhost:8080/auth/admin/master/console](http://localhost:8080/auth/admin/master/console)

## Интернационализация

Файлы для интернационализации находятся в [каталогах с темами](../keycloak-extension/src/main/resources/themes/).
Например, для кастомной темы административной консоли они расположены в [keycloak.v2/admin/messages](../keycloak-extension/src/main/resources/themes/keycloak.v2/admin/messages)

## Дополнительные источники

1. [Документация SSO](../README.MD)
2. [Документация Frontend Client](../frontend/README.MD)
3. [Документация Admin UI](./js/apps/admin-ui/README.md)
