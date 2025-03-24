--liquibase formatted sql

--changeset Marat Tim:initial runOnChange:false splitStatements:false runInTransaction:false
BEGIN;

CREATE OR REPLACE FUNCTION update_modified_column_func() RETURNS trigger AS
$$
BEGIN
    new.last_modified_at = NOW();
    RETURN new;
END;
$$ LANGUAGE 'plpgsql';

CREATE TABLE todolist
(
    id               serial primary key,
    title            text,
    todo             text,
    is_completed     boolean,
    created_at       timestamp(6) WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_modified_at timestamp(6) WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TRIGGER todolist_last_modified_at_trigger
    BEFORE UPDATE
    ON todolist
    FOR EACH ROW
EXECUTE PROCEDURE update_modified_column_func();

CREATE TABLE users
(
    username text    NOT NULL PRIMARY KEY,
    password text    NOT NULL,
    enabled  boolean NOT NULL
);

CREATE TABLE authorities
(
    username  text NOT NULL,
    authority text NOT NULL,
    FOREIGN KEY (username) REFERENCES users (username)
);

COMMIT;