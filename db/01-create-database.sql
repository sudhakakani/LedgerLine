-- Run as a PostgreSQL superuser (for example, postgres).
CREATE USER ledgerline_app WITH PASSWORD 'change_this_password';
CREATE DATABASE ledgerline_db OWNER ledgerline_app;
GRANT ALL PRIVILEGES ON DATABASE ledgerline_db TO ledgerline_app;
