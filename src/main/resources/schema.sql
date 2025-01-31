-- Generated using ChatGPT
CREATE SEQUENCE user_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE users
(
    id            BIGINT    DEFAULT nextval('user_id_seq') PRIMARY KEY,
    username      VARCHAR(255) NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name    VARCHAR(255),
    last_name     VARCHAR(255),
    image_id      BIGINT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE SEQUENCE book_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE books
(
    id           BIGINT    DEFAULT nextval('book_id_seq') PRIMARY KEY,
    title        VARCHAR(255) NOT NULL,
    author       VARCHAR(255),
    isbn         VARCHAR(13),
    description  TEXT,
    publish_date DATE,
    image_id     BIGINT,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE SEQUENCE review_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE reviews
(
    id          BIGINT    DEFAULT nextval('review_id_seq') PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    book_id     BIGINT NOT NULL,
    review_text TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE
);

CREATE SEQUENCE rating_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE ratings
(
    id         BIGINT    DEFAULT nextval('rating_id_seq') PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    book_id    BIGINT NOT NULL,
    rating     INT CHECK (rating >= 1 AND rating <= 5),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE
);

CREATE SEQUENCE bookshelf_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE bookshelves
(
    id         BIGINT    DEFAULT nextval('bookshelf_id_seq') PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    book_id    BIGINT       NOT NULL,
    shelf_name VARCHAR(255) NOT NULL, -- "To Read", "Currently Reading", "Read"
    added_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE
);

CREATE SEQUENCE reading_progress_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE reading_progress
(
    id                   BIGINT        DEFAULT nextval('reading_progress_id_seq') PRIMARY KEY,
    user_id              BIGINT NOT NULL,
    book_id              BIGINT NOT NULL,
    current_page         INT           DEFAULT 0,    -- or whatever progress unit you want
    percentage_completed DECIMAL(5, 2) DEFAULT 0.00, -- track percentage read
    last_updated         TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE
);

CREATE SEQUENCE image_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE images
(
    id           BIGINT    DEFAULT nextval('image_id_seq') PRIMARY KEY,
    image_data   BYTEA,
    image_type   VARCHAR(255),
    content_type VARCHAR(255),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indices
CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_updated_at ON users (updated_at);

CREATE INDEX idx_books_isbn ON books (isbn);
CREATE INDEX idx_books_title ON books (title);
CREATE INDEX idx_books_updated_at ON books (updated_at);

CREATE INDEX idx_reviews_user_id ON reviews (user_id);
CREATE INDEX idx_reviews_book_id ON reviews (book_id);
CREATE INDEX idx_reviews_user_book ON reviews (user_id, book_id);

CREATE INDEX idx_ratings_book_id ON ratings (book_id);
CREATE INDEX idx_ratings_user_book ON ratings (user_id, book_id);
CREATE INDEX idx_ratings_rating ON ratings (rating);

CREATE INDEX idx_bookshelves_user_id ON bookshelves (user_id);
CREATE INDEX idx_bookshelves_user_shelfname ON bookshelves (user_id, shelf_name);

CREATE INDEX idx_reading_progress_user_id ON reading_progress (user_id);
CREATE INDEX idx_reading_progress_user_book ON reading_progress (user_id, book_id);
