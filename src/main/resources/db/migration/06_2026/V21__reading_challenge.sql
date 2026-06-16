-- Reading Challenge
CREATE TABLE reading_challenge
(
    login_id   BIGINT NOT NULL,
    year       INT    NOT NULL,
    book_count INT    NOT NULL,
    CONSTRAINT pk_reading_challenge PRIMARY KEY (login_id, year),
    CONSTRAINT fk_reading_challenge_logins FOREIGN KEY (login_id) REFERENCES logins (id)
);
