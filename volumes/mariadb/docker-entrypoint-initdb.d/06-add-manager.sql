INSERT INTO USER_ENTITY (ID, EMAIL, EMAIL_CONSTRAINT, EMAIL_VERIFIED, ENABLED, FEDERATION_LINK, FIRST_NAME,
                         LAST_NAME, REALM_ID, USERNAME, CREATED_TIMESTAMP, SERVICE_ACCOUNT_CLIENT_LINK, NOT_BEFORE)
VALUES ('5f18af1d-34a0-4919-8372-93e71f87657f', 'manager@nomail.ru', 'manager@nomail.ru', true, true, null, 'manager',
        null, 'manager', 'manager@nomail.ru', 1727848731354, null, 0);

-- password=manager
INSERT INTO CREDENTIAL (ID, DEVICE, HASH_ITERATIONS, SALT, TYPE, VALUE, USER_ID, CREATED_DATE, COUNTER, DIGITS,
                        PERIOD, ALGORITHM)
VALUES ('8aca0b7e-ae26-44af-aab9-538c33a18876', null, 27500, 0xE3E89F0BA77F5057E70B9B8D6B544B91, 'password',
        'TFtETwC1faz7czI6GQHyn3BjbiEHHJx5jtmuiREOtnimXZC+/eknzf2BlZs1snnLJXJ2dKAlsowIwgyEhKbG3A==',
        '5f18af1d-34a0-4919-8372-93e71f87657f', 1727848824861, 0, 0, 0, 'pbkdf2-sha256');

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
