-- Feed
CREATE TABLE feed_entry
(
    id             BIGINT      NOT NULL,
    audience_login BIGINT      NOT NULL,
    activity_id    BIGINT      NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_feed_entry PRIMARY KEY (id),
    CONSTRAINT fk_feed_entry_audience FOREIGN KEY (audience_login) REFERENCES logins (id),
    CONSTRAINT fk_feed_entry_activity FOREIGN KEY (activity_id) REFERENCES activity (id)
);

CREATE INDEX feed_entry_idx ON feed_entry (audience_login, created_at);

CREATE SEQUENCE feed_entry_seq START WITH 1 INCREMENT BY 1;
