CREATE TABLE password_reset_token
(
    id               BIGINT        NOT NULL,
    email            VARCHAR(255)  NOT NULL,
    hashed_signature VARCHAR(1000) NOT NULL,
    generated_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    expiry_timestamp TIMESTAMPTZ   NOT NULL,
    CONSTRAINT pk_password_reset_token PRIMARY KEY (id)
);

CREATE SEQUENCE password_reset_token_seq START WITH 1 INCREMENT BY 1;
