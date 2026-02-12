CREATE TABLE activity_outbox
(
    id               SERIAL       NOT NULL,
    event_type       VARCHAR(255) NOT NULL,
    reference_entity VARCHAR(255) NOT NULL,
    reference_id     BIGINT,
    payload          JSONB        NOT NULL,
    status           VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    retry_count      INT          NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ  NOT NULL,
    processed_at     TIMESTAMPTZ,
    CONSTRAINT pk_activity_outbox PRIMARY KEY (id)
);

CREATE INDEX activity_outbox_idx ON activity_outbox (status, created_at) WHERE status = 'PENDING';

CREATE TABLE activity
(
    id               BIGINT       NOT NULL,
    login_id         BIGINT       NOT NULL,
    event_type       VARCHAR(255) NOT NULL,
    reference_entity VARCHAR(255) NOT NULL,
    reference_id     BIGINT,
    metadata         JSONB,
    created_at       TIMESTAMPTZ DEFAULT now(),
    internal         BOOLEAN     DEFAULT FALSE,
    CONSTRAINT pk_activity PRIMARY KEY (id),
    CONSTRAINT fk_activity_login FOREIGN KEY (login_id) REFERENCES logins (id)
);

CREATE SEQUENCE activity_seq START WITH 1 INCREMENT BY 1;

CREATE INDEX activity_log_created_idx ON activity (created_at);
CREATE INDEX activity_log_login_created_idx ON activity (login_id, created_at);
