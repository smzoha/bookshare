-- Author Table Creation
CREATE TABLE author
(
    id                  BIGINT       NOT NULL,
    name                VARCHAR(255) NOT NULL,
    login_id            BIGINT,
    bio                 TEXT,
    profile_picture_url VARCHAR(255),
    created_at          TIMESTAMPTZ  NOT NULL,
    updated_at          TIMESTAMPTZ,
    CONSTRAINT pk_author PRIMARY KEY (id),
    CONSTRAINT fk_author_login FOREIGN KEY (login_id) REFERENCES logins (id)
);

CREATE SEQUENCE author_seq START WITH 1 INCREMENT BY 1;

ALTER TABLE book_authors
    DROP CONSTRAINT fk_book_authors_author;

ALTER TABLE book_authors
    ADD CONSTRAINT fk_book_authors_author FOREIGN KEY (author_id) REFERENCES author (id);
