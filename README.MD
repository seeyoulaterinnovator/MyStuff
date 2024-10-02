# Дом.ру Б2Б SSO

https://gitlab.ertelecom.ru/b2bweb/sso

## Требования

> Версии ПО, на котором велась разработка

* JDK 17 и Gradle 8
* Node 20 и NPM 9 (PNPM 9)
* Docker 27

## Разработка

> Подразумевается состояние БД до миграции, т.е. на момент версии Keycloak 6.0.1

1. Пометить папку [docker-entrypoint-initdb.d](./volumes/mariadb/docker-entrypoint-initdb.d), как excluded
2. Скопировать скрипт стендовой БД dump-а в файл.
   [docker-entrypoint-initdb.d/01-dump.sql](./volumes/mariadb/docker-entrypoint-initdb.d/01-dump.sql)
   (при создании нового dump-а можно исключить ADMIN_EVENT_ENTITY, USER_LOGIN_HISTORY, EVENT_ENTITY, 
   но не забыть потом их создать).
3. Выполнить сборку проекта скриптом [build.sh](./build.sh) (frontend и backend).
4. Запустить [docker-compose-local.yaml](./docker-compose-local.yml) в одном из режимов в зависимости 
   от необходимых доработок через соответствующий Intellij Idea run config.

### Учетные данные для входа

> См. дополнительные скрипты для патча БД [\d\d-\w+.sql](./volumes/mariadb/docker-entrypoint-initdb.d/)

* Администратор - admin / admin
* Менеджер - manager@nomail.ru / manager

### Адреса

* Консоль администратора - [/admin/master/console](http://localhost:8080/auth/admin/master/console/)
* Консоль менеджера - [/admin/manager/console](http://localhost:8080/auth/admin/manager/console/)

## Разработка только backend

### Run config-и:

* *docker-compose-full* - запуск БД, Keycloak и mock-сервисов и прочего окружения для разработки.
* *docker-compose* - запуск только БД, Keycloak.
* *docker-compose-config-cli* - перезапуск config CLI утилиты для ускоренной отладки тестового realm-а для E2E.
  перед написанием теста.

Обновление путем пересборки через Gradle и перезапуска сервиса keycloak из docker-compose.

## Разработка frontend

### Административный интерфейс

Модуль [admin-ui](./frontend-admin/js/apps/admin-ui).

Запуск Vite dev сервера через run config "keycloak.v2 admin dev".

Запуск docker-compose через run config-и *docker-compose-full-ui* или *docker-compose-ui*.

Интерфейс доступен с основного веб сервера Keycloak, т.к. производится обратное проксирование на dev сервер.


### Пользовательский интерфейс

Модуль [domru](./frontend/domru).

Запуск автопересборки через run config "domru autobuild".

Запуск docker-compose в любом варианте.

Интерфейс доступен с основного веб сервера Keycloak, т.к. dev сервера нет.

## E2E

Модуль [e2e](./e2e).

Основная идея: Keycloak вместе со всем окружением развертывается через test containers, 
к нему применяется через config CLI утилиту заранее подготовленный тестовый realm в виде минимизированного абстрактного
import файла [e2e.json](./volumes/keycloak-config-cli/config/e2e.json).

## Заливка на стенд

```text
TODO
```

## Заливка на прод

```text
TODO
```

## Ключевые таблицы БД

* *ADMIN_EVENT_ENTITY* - поможет отследить что делал менеджер в админке
* *APP_PROPERTIES* - настройки проекта, разные подключения
* *AUTO_LOCK_NOTIFICATION* - таблица планировщика по предупреждения по долгой не активности
* *CLIENT* - все клиенты для разных realm-ов
* *IMPORT_USERS_DATA*
* *IMPORT_USERS_REPORT* - файлы отвечающие за миграцию пользователей
* *REALM* - настройки realm-а
* *USER_ENTITY* - все пользователи разных realm-ов
* *USER_POST* - таблица customer-ов пользователей
* *USER_ATTRIBUTE* - атрибуты пользователей

## Сервисные письма на email

Изменения производятся в [themes/domru/email/html](keycloak-extension/src/main/resources/themes/domru/email/html).

