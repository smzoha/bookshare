-- Logins
ALTER TABLE logins
    ADD COLUMN active BOOLEAN NOT NULL DEFAULT FALSE;

-- Book
ALTER TABLE book
    ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE book
    ALTER COLUMN status DROP DEFAULT;

-- Follow (Pending)
CREATE TABLE pending_follow
(
    id           BIGINT NOT NULL,
    follower_id  BIGINT NOT NULL,
    following_id BIGINT NOT NULL,
    CONSTRAINT pk_pending_follow PRIMARY KEY (id),
    CONSTRAINT fk_pending_follow_follower FOREIGN KEY (follower_id) REFERENCES logins (id),
    CONSTRAINT fk_pending_follow_following FOREIGN KEY (following_id) REFERENCES logins (id),
    CONSTRAINT uk_pending_follow UNIQUE (follower_id, following_id)
);

-- Review/Like
CREATE TABLE review_likes
(
    review_id BIGINT NOT NULL,
    login_id  BIGINT NOT NULL,
    CONSTRAINT pk_review_likes PRIMARY KEY (review_id, login_id),
    CONSTRAINT fk_review_likes_review FOREIGN KEY (review_id) REFERENCES review (id),
    CONSTRAINT fk_review_likes_login FOREIGN KEY (login_id) REFERENCES logins (id)
);
