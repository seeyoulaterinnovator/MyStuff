update REALM
set ssl_required = 'NONE';

update APP_PROPERTIES
set value = 'http://mockserver:1080/cities/domains'
where name = 'cities.url';

update REALM_SMTP_CONFIG
set VALUE = 'smtp4dev'
where NAME = 'host';

update SETTINGS
set VALUE = 'http://mockserver:1080/dadata/suggestions/api/4_1/rs/iplocate/address'
where EXT_ID = 'urlDaDataRequestLocationIp';

update CLIENT
set SECRET = 'secret'
where CLIENT_ID in ('b2b') and REALM_ID = 'user';

delete from REDIRECT_URIS
where CLIENT_ID in (select ID
                    from CLIENT
                    where CLIENT_ID in ('b2b') and REALM_ID = 'user');

INSERT INTO REDIRECT_URIS(CLIENT_ID, VALUE)
select ID, '*'
from CLIENT
where CLIENT_ID in ('b2b') and REALM_ID = 'user';

update APP_PROPERTIES
set VALUE = 'mockserver'
where NAME in ('tbapi.registration.ip', 'tbapi.registration.host', 'tbapi.customer.ip', 'tbapi.customer.host');
update APP_PROPERTIES
set VALUE = '1080'
where NAME in ('tbapi.registration.port', 'tbapi.customer.port');
UPDATE APP_PROPERTIES
SET VALUE = 'false'
WHERE NAME IN ('tbapi.registration.secure', 'tbapi.customer.secure');
update APP_PROPERTIES
set VALUE = '/tbapi/api/v1/customerManagement/customerAccount'
where NAME = 'tbapi.registration.find.path';
update APP_PROPERTIES
set VALUE = '/tbapi/api/v1/customerManagement/customerAccounts/names'
where NAME = 'tbapi.customer.find.path';

update REALM set ADMIN_THEME = 'keycloak.v2';

insert into SETTINGS
select uuid() as ID, EXT_ID, VALUE, `DESC`, 'e2e' as REALM_ID, NAME, UNIT, TYPE
from SETTINGS s
where REALM_ID = 'user';

