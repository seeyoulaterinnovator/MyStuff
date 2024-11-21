INSERT INTO USER_ENTITY
(ID, EMAIL, EMAIL_CONSTRAINT, EMAIL_VERIFIED, ENABLED, FEDERATION_LINK, FIRST_NAME, LAST_NAME, REALM_ID, USERNAME,
 CREATED_TIMESTAMP, SERVICE_ACCOUNT_CLIENT_LINK, NOT_BEFORE)
VALUES ('e31137b0-39fc-4f3b-a62f-7b048eed1230', 'admin@nomail.ru', 'admin@nomail.ru', 1, 1, NULL, 'admin', NULL,
        'master', 'admin', 1724331178659, NULL, 0);

-- password=admin
INSERT INTO CREDENTIAL (ID, SALT, TYPE, USER_ID, CREATED_DATE, USER_LABEL, SECRET_DATA, CREDENTIAL_DATA, PRIORITY)
VALUES ('af9d70c8-a75a-4cb7-865d-4b7e88219109', null, 'password', 'e31137b0-39fc-4f3b-a62f-7b048eed1230', 1732099524427, 'My password', '{"value":"OtJwiTMkaWfxJPzY4J/SH3lgyxdNsoMHIzxj4paZfAc=","salt":"aZSWPVjMi9v3ZHjhCintWg==","additionalParameters":{}}', '{"hashIterations":5,"algorithm":"argon2","additionalParameters":{"hashLength":["32"],"memory":["7168"],"type":["id"],"version":["1.3"],"parallelism":["1"]}}', 10);


insert into USER_ROLE_MAPPING(ROLE_ID, USER_ID)
values ((select id from KEYCLOAK_ROLE where NAME = 'admin'),
        (select id from USER_ENTITY where USERNAME = 'admin'));
