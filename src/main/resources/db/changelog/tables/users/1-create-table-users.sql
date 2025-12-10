-- liquibase formatted sql

-- changeset red-eyed:create-table-users

CREATE TABLE users
(
    id       UUID PRIMARY KEY DEFAULT UUID_GENERATE_V4(),
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL

    CONSTRAINT check_username CHECK (
        username ~ '^[a-zA-Z0-9]+[a-zA-Z_0-9]*[a-zA-Z0-9]+$' AND
        LENGTH(username) BETWEEN 5 AND 20
    )

    CONSTRAINT check_password_bcrypt_encoded CHECK (
        password ~ '\A\$2(a|y|b)?\$(\d\d)\$[./0-9A-Za-z]{53}'
    )
);