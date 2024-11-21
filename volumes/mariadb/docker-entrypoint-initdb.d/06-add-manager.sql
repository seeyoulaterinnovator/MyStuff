INSERT INTO USER_ENTITY (ID, EMAIL, EMAIL_CONSTRAINT, EMAIL_VERIFIED, ENABLED, FEDERATION_LINK, FIRST_NAME,
                         LAST_NAME, REALM_ID, USERNAME, CREATED_TIMESTAMP, SERVICE_ACCOUNT_CLIENT_LINK, NOT_BEFORE)
VALUES ('5f18af1d-34a0-4919-8372-93e71f87657f', 'manager@nomail.ru', 'manager@nomail.ru', true, true, null, 'manager',
        null, 'manager', 'manager@nomail.ru', 1727848731354, null, 0);

-- password=manager
INSERT INTO CREDENTIAL (ID, SALT, TYPE, USER_ID, CREATED_DATE, USER_LABEL, SECRET_DATA, CREDENTIAL_DATA, PRIORITY)
VALUES ('3c899d2a-dc26-4344-bbed-5457970f415a', null, 'password', '5f18af1d-34a0-4919-8372-93e71f87657f', 1732099700539, 'My password',
        '{"value":"EnDApHjOOFUBdPCpGIuC6IadN4J96U5dfWp/4iap5w0=","salt":"hIO6r09/QlCLPsBNdVEr9A==","additionalParameters":{}}',
        '{"hashIterations":5,"algorithm":"argon2","additionalParameters":{"hashLength":["32"],"memory":["7168"],"type":["id"],"version":["1.3"],"parallelism":["1"]}}', 10);


insert into USER_ROLE_MAPPING(ROLE_ID, USER_ID)
select ID as ROLE_ID, '5f18af1d-34a0-4919-8372-93e71f87657f' as USER_ID
from KEYCLOAK_ROLE
where REALM_ID = 'manager'
  and NAME in (
               'view-users',
               'view-identity-providers',
               'manage-users',
               'impersonation',
               'edit-attributes',
               'edit-groups',
               'edit-sessions',
               'edit-role-mappings',
               'edit-customer',
               'edit-federated-identity',
               'edit-consents',
               'edit-details',
               'edit-credentials',
               'button-add-customer',
               'button-delete-customer',
               'required-actions',
               'button-add-user',
               'button-block-users',
               'button-download-template-csv',
               'button-download-template-xlsx',
               'button-export-csv',
               'button-export-xlsx',
               'button-import-file-csv',
               'button-reset-password',
               'button-unlock-users'
    );
