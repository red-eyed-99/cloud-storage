-- liquibase formatted sql

-- changeset red-eyed:create-enum-resource-type

CREATE TYPE RESOURCE_TYPE AS ENUM ('FILE', 'DIRECTORY');