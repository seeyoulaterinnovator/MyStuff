create index if not exists IX_CUSTOMER_NAME on CUSTOMER(NAME);

SET @realm = 'user';
SET @userCount = 1000;
SET @idPrefix = 'abc1fdcd-a177-11ef-b1b4-'; -- SUBSTRING(uuid(), 1, 24)
SET @phonePrefix = (
    WITH RECURSIVE nums(i) AS (
    SELECT 1
    UNION ALL
    SELECT i + 1 FROM nums
    WHERE i < 999 and EXISTS (
    select * from USER_ATTRIBUTE ua
    join USER_ENTITY ue on ua.USER_ID = ue.ID
    where ua.name = 'phone'
    and REALM_ID = 'user'
    and value like concat('7', cast(lpad(cast(i as char), 3, '0') as CHAR), '%')
    )
    )
    select concat('7', cast(lpad(cast(i as char), 3, '0') as CHAR)) from nums
    limit 1
    );

INSERT INTO USER_ENTITY(
    ID, EMAIL, EMAIL_CONSTRAINT, EMAIL_VERIFIED, ENABLED, FEDERATION_LINK,
    FIRST_NAME, LAST_NAME, REALM_ID, USERNAME, CREATED_TIMESTAMP,
    SERVICE_ACCOUNT_CLIENT_LINK, NOT_BEFORE
)
WITH RECURSIVE nums(i) AS (
    SELECT 1
    UNION ALL
    SELECT i + 1 FROM nums
    WHERE i < @userCount
),
               ids as (
                   SELECT
                       concat(@idPrefix, cast(lpad(cast(i as char), 12, '0') as CHAR)) as ID
                   FROM nums
               )
select
    ID,
    concat(ID, '@autogen.test') as EMAIL,
    concat(ID, '@autogen.test') as EMAIL_CONSTRAINT,
    1 as EMAIL_VERIFIED,
    1 as ENABLED,
    null as FEDERATION_LINK,
    concat('Tester-', ID) as FIRST_NAME,
    concat('Testov-', ID) as LAST_NAME,
    @realm as REALM_ID,
    concat('tester-', ID) as USERNAME,
    UNIX_TIMESTAMP() * 1000 as CREATED_TIMESTAMP,
    null as SERVICE_ACCOUNT_CLIENT_LINK,
    0 NOT_BEFORE
from ids;

insert into USER_ATTRIBUTE(ID, USER_ID, NAME, VALUE)
select
    concat(SUBSTRING(uuid(), 1, 24), cast(lpad(row_number() over (order by ue.ID), 12, '0') as CHAR)) as ID,
    ue.ID as USER_ID,
    'phone' as NAME,
    concat(@phonePrefix, cast(lpad(row_number() over (order by ue.ID), 7, '0') as CHAR)) as VALUE
from USER_ENTITY ue
where ID like concat(@idPrefix, '%') and REALM_ID = @realm;

insert into CREDENTIAL(ID, SALT, `TYPE`, USER_ID, CREATED_DATE, USER_LABEL, SECRET_DATA, CREDENTIAL_DATA, PRIORITY)
select
    concat(SUBSTRING(uuid(), 1, 24), cast(lpad(row_number() over (order by ue.ID), 12, '0') as CHAR)) as ID,
    NULL as SALT,
    'password' as `TYPE`,
        ue.ID as USER_ID,
    UNIX_TIMESTAMP() * 1000 as CREATED_DATE,
    'My password' as USER_LABEL,
    '{"value":"U2RGAh/LkdI2bEwTsxWyUN5Fj6lnw/qP0ICV+LlD/wk=","salt":"pB/5Q2L3YVOueEeqeHtoDQ==","additionalParameters":{}}' as SECRET_DATA,
    '{"hashIterations":5,"algorithm":"argon2","additionalParameters":{"hashLength":["32"],"memory":["7168"],"type":["id"],"version":["1.3"],"parallelism":["1"]}}' as CREDENTIAL_DATA,
    10 as PRIORITY
from USER_ENTITY ue
where ue.ID like concat(@idPrefix, '%') and ue.REALM_ID = @realm;

select * from CUSTOMER;

INSERT INTO CUSTOMER (ID, NAME, update_time)
select
    UNIX_TIMESTAMP() + (row_number() over (order by ue.ID)) as ID,
    ue.ID as NAME,
    NOW()
from USER_ENTITY ue
where ue.ID like concat(@idPrefix, '%') and ue.REALM_ID = @realm;

insert into USER_POST(id, user_id, toms_id, dmp_id, role_id)
select
    concat(SUBSTRING(uuid(), 1, 24), cast(lpad(row_number() over (order by ue.ID), 12, '0') as CHAR)) as ID,
    ue.ID as USER_ID,
    c.ID as toms_id,
    concat(SUBSTRING(uuid(), 1, 24), cast(lpad(row_number() over (order by ue.ID), 12, '0') as CHAR)) as dmp_id,
    (select ID from USER_POST_ROLE where NAME = 'LPR') as role_id
from USER_ENTITY ue
         join CUSTOMER c on cast(c.NAME as CHAR) = ue.ID
where ue.ID like concat(@idPrefix, '%') and ue.REALM_ID = @realm;


INSERT INTO USERPOST_EXT_SYSTEM_ROLE(USER_POST_ID, EXT_SYSTEM_ROLE_ID)
select
    up.ID as USER_POST_ID,
    esr.ID as EXT_SYSTEM_ROLE_ID
from USER_POST up
         join USER_ENTITY ue on ue.ID = up.USER_ID
         cross join EXT_SYSTEM_ROLE esr
where ue.ID like concat(@idPrefix, '%') and ue.REALM_ID = @realm and esr.REALM_ID = @realm;
