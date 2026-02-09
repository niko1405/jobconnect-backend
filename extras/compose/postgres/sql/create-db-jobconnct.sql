-- https://www.postgresql.org/docs/current/sql-createuser.html
-- https://www.postgresql.org/docs/current/sql-createrole.html
CREATE USER jobconnect PASSWORD 'p';

-- https://www.postgresql.org/docs/current/sql-createdatabase.html
CREATE DATABASE jobconnect;

-- https://www.postgresql.org/docs/current/role-attributes.html
-- https://www.postgresql.org/docs/current/ddl-priv.html
-- https://www.postgresql.org/docs/current/sql-grant.html
GRANT ALL ON DATABASE jobconnect TO jobconnect;

-- https://www.postgresql.org/docs/current/sql-createtablespace.html
CREATE TABLESPACE jobconnectspace OWNER jobconnect LOCATION '/var/lib/postgresql/tablespace/jobconnect';
