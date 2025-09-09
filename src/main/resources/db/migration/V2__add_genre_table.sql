-- Genre
CREATE TABLE IF NOT EXISTS genre
(
    id         BIGINT       NOT NULL,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL,
    CONSTRAINT pk_genre PRIMARY KEY (id)
);

CREATE SEQUENCE genre_seq START WITH 1 INCREMENT BY 1;

-- Book/Genre
CREATE TABLE book_genre
(
    book_id BIGINT NOT NULL,
    genre_id  BIGINT NOT NULL,
    CONSTRAINT pk_book_genre PRIMARY KEY (book_id, genre_id),
    CONSTRAINT fk_book_genre_book FOREIGN KEY (book_id) REFERENCES book (id),
    CONSTRAINT fk_book_genre_genre FOREIGN KEY (genre_id) REFERENCES genre (id)
);
