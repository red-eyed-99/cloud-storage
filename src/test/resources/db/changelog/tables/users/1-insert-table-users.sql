-- liquibase formatted sql

-- changeset red-eyed:insert-table-users

-- password - 12345
INSERT INTO users
VALUES ('aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee', 'test_user', '$2a$10$Ij9oAsgSkxX8BVyNFAl2HeIg2ywDRduOrVyN24uXTejnCM9clqyiS')