-- Logins
ALTER TABLE logins
    ALTER COLUMN last_name DROP NOT NULL;

-- Review
CREATE TABLE review_like
(
    login_id  BIGINT NOT NULL,
    review_id BIGINT NOT NULL,
    CONSTRAINT pk_review_like PRIMARY KEY (login_id, review_id),
    CONSTRAINT fk_review_like_login FOREIGN KEY (login_id) REFERENCES logins (id),
    CONSTRAINT fk_review_like_review FOREIGN KEY (review_id) REFERENCES review (id)
);
