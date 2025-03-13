# Дом.ру Б2Б SSO

https://gitlab.ertelecom.ru/b2bweb/sso

## Требования

> Версии ПО, на котором велась разработка

* JDK 17 и Gradle 8
* Node 20 и NPM 9 (PNPM 9)
* Docker 27

## Разработка

> Проверено на БД Keycloak-а версии 25.0.2

1. Пометить папку [docker-entrypoint-initdb.d](./volumes/mariadb/docker-entrypoint-initdb.d), как excluded
2. Скопировать скрипт стендовой БД dump-а в файл.
   [docker-entrypoint-initdb.d/01-dump.sql](./volumes/mariadb/docker-entrypoint-initdb.d/01-dump.sql)
   (при создании нового dump-а можно исключить ADMIN_EVENT_ENTITY, USER_LOGIN_HISTORY, EVENT_ENTITY, 
   но не забыть потом их создать).
3. Выполнить сборку проекта скриптом [build.sh](./build.sh) (frontend и backend).
4. Запустить [docker-compose-local.yaml](./docker-compose-local.yml) в одном из режимов в зависимости 
   от необходимых доработок через соответствующий Intellij Idea run config.

### Адреса

> Учетные данные для входа cм. в дополнительных скриптах для патча БД 
> [\d\d-\w+.sql](./volumes/mariadb/docker-entrypoint-initdb.d/)

* Консоль администратора (admin / admin) - [/admin/master/console](http://localhost:8080/auth/admin/master/console/).
* Консоль менеджера (manager@nomail.ru / manager) - [/admin/manager/console](http://localhost:8080/auth/admin/manager/console/).
* MBean кэша подключиться по протоколу JMX через JConsole к `service:jmx:rmi:///jndi/rmi://127.0.0.1:9990/jmxrmi`,
  см. MBeans > "org.infinispan" (напр. сброс сессий операция `clear` для bean-а 
  `org.infinispan:type=Cache,name="sessions(dist_sync)",manager="custom_container",component=Cache`)

## Разработка только backend

### Run config-и:

* *docker-compose-full* - запуск БД, Keycloak и mock-сервисов и прочего окружения для разработки.
* *docker-compose* - запуск только БД, Keycloak.
* *docker-compose-config-cli* - перезапуск config CLI утилиты для ускоренной отладки тестового realm-а для E2E.
  перед написанием теста.

Обновление путем пересборки через Gradle и перезапуска сервиса keycloak из docker-compose.

## Разработка frontend

Keycloak обеспечивает поддержку тем для веб-страниц и электронных писем. Это позволяет настраивать внешний вид страниц, доступных конечному пользователю, чтобы их можно было интегрировать с вашими приложениями.

Тема может предоставлять один или несколько типов для настройки различных аспектов Keycloak. Доступные типы:
- Account - Account Console
- Admin - Admin Console
- Email - Emails
- Login - Login forms
- Welcome - Welcome page

В рамках данного проекта конфигурируются следующие типы тем: *Admin*, *Login*, *Email*.

- Конфигурирование тем *Login* и *Email* основывается на редактировании [FreeMarker](https://freemarker.apache.org/) шаблонов, определенных в [themes](./keycloak-extension/src/main/resources/themes)
- Конфигурирование темы *Admin* основывается на разработке [React приложения](./frontend-admin/js/apps/admin-ui). Подробнее с кастомизацией тем, основанных на React, можно ознакомиться в [документации](https://www.keycloak.org/docs/latest/server_development/#_theme_react). 
⚠️ _На момент разработки собственной темы административной консоли пакета *@keycloak/keycloak-admin-ui* не существовало, хотя он и был описан в документации. В связи с этим было найдено [альтернативное решение](https://github.com/keycloak/keycloak/discussions/32645#discussioncomment-10566232)._

Подробнее с кастомизацией тем можно ознакомиться в [документации](https://www.keycloak.org/docs/latest/server_development/#_themes).

### Admin Console - Административный интерфейс

[Документация Frontend Admin](./frontend-admin/README.MD)

<!-- Запуск Vite dev сервера через run config "keycloak.v2 admin dev".

Запуск docker-compose через run config-и *docker-compose-full-ui* или *docker-compose-ui*.

Интерфейс доступен с основного веб сервера Keycloak, т.к. производится обратное проксирование на dev сервер. -->

### Login Form - Пользовательский интерфейс

[Документация Frontend Client](./frontend/README.MD)

<!-- Запуск автопересборки через run config "domru autobuild".

Запуск docker-compose в любом варианте.

Интерфейс доступен с основного веб сервера Keycloak, т.к. dev сервера нет. -->

### Email - Почтовые сообщения

См. [Сервисные письма на email](#Сервисные-письма-на-email)

## E2E

Модуль [e2e](./e2e).

Основная идея: Keycloak вместе со всем окружением развертывается через test containers, 
к нему применяется через config CLI утилиту заранее подготовленный тестовый realm в виде минимизированного абстрактного
import файла [e2e.json](./volumes/keycloak-config-cli/config/e2e.json).

Для дополнения тестовых realm можно сделать полный экспорт:
```shell
export JAVA_OPTS="" && /opt/keycloak/bin/kc.sh export --file /tmp/realm-export.json --realm user
```

Для отладки теста можно запускать его поверх контейнеров запущенных через *docker-compose-full-e2e* 
(отключен кэш настроек) для этого можно временно убрать аннотацию *@TestsEnabled* и запустить тест индивидуально.

## Заливка стенда

```text
TODO
```

## Обновление продуктива

> Есть замещение провайдера Liquibase для ProxySQL [./UpdatedMariaDBDatabase.java](./keycloak-extension/src/main/java/org/keycloak/connections/jpa/updater/liquibase/UpdatedMariaDBDatabase.java)

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

Конфигурирование почтовых сообщений производится в [themes/{yourTheme}/email](keycloak-extension\src\main\resources\themes\domru\email) и сводится к корректировке стилей и FreeMarker шаблонов. При разработке стоит учитывать [особенности верстки](https://habr.com/ru/articles/252279/) для почтовых клиентов. Для проверки поддержки той или иной функции почтовым клиентом рекомендуется использовать сервис [Can I email](https://www.caniemail.com/features/).

Для перехвата почтовых сообщений при локальной разработке используется *smtp4dev* сервер.

### Стенды 

- **dev**: [https://smtp4dev-er-sso.dev.it-resource.team/](https://smtp4dev-er-sso.dev.it-resource.team/)

- **local**: [http://localhost:8025](http://localhost:8025/)
