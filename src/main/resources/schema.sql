-- Login
CREATE TABLE logins
(
    id                  BIGINT        NOT NULL,
    email               VARCHAR(255)  NOT NULL,
    name                VARCHAR(255),
    password            VARCHAR(1024) NOT NULL,
    role                VARCHAR(255)  NOT NULL,
    bio                 TEXT,
    profile_picture_url VARCHAR(255),
    created_at          TIMESTAMPTZ   NOT NULL,
    updated_at          TIMESTAMPTZ,
    CONSTRAINT pk_login PRIMARY KEY (id),
    CONSTRAINT uk_login_email UNIQUE (email)
);

CREATE SEQUENCE login_seq START WITH 1 INCREMENT BY 1;

-- Book
CREATE TABLE book
(
    id               BIGINT       NOT NULL,
    title            VARCHAR(255) NOT NULL,
    isbn             VARCHAR(50)  NOT NULL,
    description      TEXT,
    cover_image_url  VARCHAR(255),
    publication_date DATE,
    created_at       TIMESTAMPTZ  NOT NULL,
    updated_at       TIMESTAMPTZ,
    CONSTRAINT pk_book PRIMARY KEY (id),
    CONSTRAINT uk_book_isbn UNIQUE (isbn)
);

-- Book/Author
CREATE TABLE book_authors
(
    book_id   BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    CONSTRAINT pk_book_authors PRIMARY KEY (book_id, author_id),
    CONSTRAINT fk_book_authors_book FOREIGN KEY (book_id) REFERENCES book (id),
    CONSTRAINT fk_book_authors_author FOREIGN KEY (author_id) REFERENCES logins (id)
);

-- Tag
CREATE TABLE tag
(
    id   BIGINT       NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT pk_tag PRIMARY KEY (id)
);

-- Book/Tag
CREATE TABLE book_tags
(
    book_id BIGINT NOT NULL,
    tag_id  BIGINT NOT NULL,
    CONSTRAINT pk_book_tags PRIMARY KEY (book_id, tag_id),
    CONSTRAINT fk_book_tags_book FOREIGN KEY (book_id) REFERENCES book (id),
    CONSTRAINT fk_book_tags_tag FOREIGN KEY (tag_id) REFERENCES tag (id)
);

-- Review
CREATE TABLE review
(
    id          BIGINT      NOT NULL,
    content     TEXT        NOT NULL,
    rating      INT,
    review_date TIMESTAMPTZ NOT NULL,
    login_id    BIGINT      NOT NULL,
    book_id     BIGINT      NOT NULL,
    CONSTRAINT pk_review PRIMARY KEY (id),
    CONSTRAINT fk_review_login FOREIGN KEY (login_id) REFERENCES logins (id),
    FOREIGN KEY (book_id) REFERENCES book (id)
);

-- Shelf
CREATE TABLE shelf
(
    id       BIGINT       NOT NULL,
    name     VARCHAR(255) NOT NULL,
    login_id BIGINT       NOT NULL,
    CONSTRAINT pk_shelf PRIMARY KEY (id),
    CONSTRAINT fk_shelf_login FOREIGN KEY (login_id) REFERENCES logins (id)
);

-- Shelf/Book
CREATE TABLE book_shelf
(
    shelf_id BIGINT NOT NULL,
    book_id  BIGINT NOT NULL,
    CONSTRAINT pk_book_shelf PRIMARY KEY (shelf_id, book_id),
    CONSTRAINT fk_book_shelf_book FOREIGN KEY (book_id) REFERENCES book (id),
    CONSTRAINT fk_book_shelf_shelf FOREIGN KEY (shelf_id) REFERENCES shelf (id)
);

-- Follow
CREATE TABLE follow
(
    id           BIGINT NOT NULL,
    follower_id  BIGINT NOT NULL,
    following_id BIGINT NOT NULL,
    CONSTRAINT pk_follow PRIMARY KEY (id),
    CONSTRAINT fk_follow_follower FOREIGN KEY (follower_id) REFERENCES logins (id),
    CONSTRAINT fk_follow_following FOREIGN KEY (following_id) REFERENCES logins (id),
    CONSTRAINT uk_follow UNIQUE (follower_id, following_id)
);

-- Sequences
CREATE SEQUENCE book_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE review_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE shelf_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE tag_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE follow_seq START WITH 1 INCREMENT BY 1;
