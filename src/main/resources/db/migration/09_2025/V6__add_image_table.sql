-- Image
CREATE TABLE image
(
    id           BIGINT       NOT NULL,
    file_name    VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    content      BYTEA        NOT NULL,
    upload_date  TIMESTAMPTZ  NOT NULL,
    CONSTRAINT pk_image PRIMARY KEY (id)
);

CREATE SEQUENCE image_seq START WITH 1 INCREMENT BY 1;

ALTER TABLE book DROP COLUMN cover_image_url;
ALTER TABLE book ADD COLUMN image_id BIGINT;
ALTER TABLE book ADD CONSTRAINT fk_book_image FOREIGN KEY (image_id) REFERENCES image (id);
