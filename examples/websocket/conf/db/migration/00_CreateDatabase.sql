create user websocket with login nosuperuser createdb nocreaterole inherit noreplication connection limit -1 password 'websocket';

create database websocket with owner = websocket encoding = 'utf8' connection limit = -1;
