insert ignore into CUSTOMER(id, name, update_time) values (10002408221, 'tester', now());
insert ignore into USER_POST(id, user_id, toms_id, dmp_id, role_id)
values (uuid(), (select id from USER_ENTITY where REALM_ID = 'e2e' and EMAIL = 'tester@nomail.tld'), 10002408221, uuid(), 1);