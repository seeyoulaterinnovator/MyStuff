# Frontend Client

## Оглавление

1. [Стенды](#Описание-и-структура-проекта)
2. [Описание и структура проекта](#Описание-и-структура-проекта)
3. [Требования](#Требования)
4. [Локальный запуск проекта](#Локальный-запуск-проекта)
5. [Интернационализация](#Интернационализация)
6. [Дополнительные источники](#Дополнительные-источники)

## Стенды 

- **prod**: [https://auth.dom.ru](https://auth.dom.ru/auth/realms/user/protocol/openid-connect/auth?city=yar&response_type=code&redirect_uri=https%3A%2F%2Fperm.b2b.dom.ru&client_id=b2b&scope=openid)

- **test**: [https://sso-balancer4.testing.srv.loc](https://sso-balancer4.testing.srv.loc/auth/realms/user/protocol/openid-connect/auth?city=yar&response_type=code&redirect_uri=https%3A%2F%2Fperm.b2b.dom.ru&client_id=b2b&scope=openid)

- **dev**: [https://er-sso.dev.it-resource.team](https://er-sso.dev.it-resource.team/auth/realms/user/protocol/openid-connect/auth?city=yar&response_type=code&redirect_uri=https%3A%2F%2Fperm.b2b.dom.ru&client_id=b2b&scope=openid)

### Дополнительные URL параметры: 
- iframe=1
- hiddenHeader=1 

## Описание и структура проекта 

- Основное конфигурирование тем клиентской части основывается на редактировании [FreeMarker](https://freemarker.apache.org/) шаблонов, определенных в [keycloak-extension\src\main\resources\themes](./keycloak-extension/src/main/resources/themes)
- В каталоге [frontend](../frontend/) содержатся Svelte-проекты для каждой из тем, которые расширяют основной функционал FreeMarker шаблонов.
- В каталоге [frontend\StubPage](./StubPage) содержатся страницы-заглушки для каждой из тем, которые используются на случай технических работ.

## Требования

- **Node** v20 (например, 20.5.0)
- **npm** v9 (например, 9.8.0)

## Локальный запуск проекта

Представленная инструкция характерна для любой темы из каталога [frontend](../frontend/).
1. Запустить сервер в соответствии с [инструкцией Разработка](../README.MD/#Разработка)
2. Запустить run config: `docker-compose-full-ui` или `docker-compose-ui`.
3. Закомментировать содержимое [.npmrc](./domru/.npmrc) ⚠️не забыть раскомментировать при коммите
4. Установить зависимости: `npm i`
5. Запустить проект: `npm run dev`
6. Открыть [localhost:8080/auth/admin/master/console](http://localhost:8080/auth/realms/user/protocol/openid-connect/auth?city=yar&response_type=code&redirect_uri=https%3A%2F%2Fperm.b2b.dom.ru&client_id=b2b&scope=openid)

## Интернационализация

Файлы для интернационализации находятся в [каталогах с темами](../keycloak-extension/src/main/resources/themes/).
Например, для кастомной темы _domru_ они расположены в [domru/login/messages](../keycloak-extension/src/main/resources/themes/domru/login/messages/)

## Дополнительные источники

1. [Документация SSO](../README.MD)
2. [Документация Frontend Admin](../frontend-admin/README.MD)