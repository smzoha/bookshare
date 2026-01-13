ALTER TABLE shelved_book DROP COLUMN pages_read;
ALTER TABLE shelved_book DROP COLUMN updated_at;

CREATE SEQUENCE reading_prog_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE reading_progress
(
    id         BIGINT    NOT NULL,
    user_id    BIGINT    NOT NULL,
    book_id    BIGINT    NOT NULL,
    pages_read BIGINT    NOT NULL DEFAULT 0,
    start_date TIMESTAMP NOT NULL,
    end_date   TIMESTAMP,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT pk_reading_progress PRIMARY KEY (id),
    CONSTRAINT fk_reading_progress_user FOREIGN KEY (user_id) REFERENCES logins (id),
    CONSTRAINT fk_reading_progress_book FOREIGN KEY (book_id) REFERENCES book (id)
);
