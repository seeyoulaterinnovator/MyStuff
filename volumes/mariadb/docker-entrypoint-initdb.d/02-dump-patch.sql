update REALM
set ssl_required = 'NONE';

update APP_PROPERTIES
set value = 'http://localhost:1080/cities/domains'
where name = 'cities.url';

update REALM_SMTP_CONFIG
set VALUE = 'localhost'
where NAME = 'host';

update SETTINGS
set VALUE = 'http://localhost:1080/dadata/suggestions/api/4_1/rs/iplocate/address'
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
set VALUE = 'localhost:1080'
where NAME in ('tbapi.registration.ip', 'tbapi.registration.host');

update APP_PROPERTIES
set VALUE = '/tbapi/api/v1/customerManagement/customerAccount'
where NAME = 'tbapi.registration.find.path';
