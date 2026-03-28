CREATE TABLE author_request
(
    id       BIGINT       NOT NULL,
    login_id BIGINT       NOT NULL,
    CONSTRAINT pk_author_application PRIMARY KEY (id),
    CONSTRAINT fk_author_application FOREIGN KEY (login_id) REFERENCES logins (id)
);

CREATE SEQUENCE author_request_seq START WITH 1 INCREMENT BY 1;
