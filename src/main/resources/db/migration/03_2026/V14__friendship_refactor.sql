-- Drop existing follow tables
DROP TABLE IF EXISTS follow;
DROP TABLE IF EXISTS pending_follow;

DROP SEQUENCE IF EXISTS follow_seq;

-- Connection
CREATE TABLE connection
(
    id         BIGINT NOT NULL,
    person1_id BIGINT NOT NULL,
    person2_id BIGINT NOT NULL,
    CONSTRAINT pk_connection PRIMARY KEY (id),
    CONSTRAINT pk_person1_id FOREIGN KEY (person1_id) REFERENCES logins (id),
    CONSTRAINT pk_person2_id FOREIGN KEY (person2_id) REFERENCES logins (id),
    CONSTRAINT chk_connection CHECK (person1_id <> person2_id)
);

CREATE TABLE friend_request
(
    id         BIGINT NOT NULL,
    person1_id BIGINT NOT NULL,
    person2_id BIGINT NOT NULL,
    CONSTRAINT pk_friend_request PRIMARY KEY (id),
    CONSTRAINT pk_person1_id FOREIGN KEY (person1_id) REFERENCES logins (id),
    CONSTRAINT pk_person2_id FOREIGN KEY (person2_id) REFERENCES logins (id),
    CONSTRAINT chk_friend_request CHECK (person1_id <> person2_id)
);

CREATE UNIQUE INDEX uq_friend_request ON friend_request (LEAST(person1_id, person2_id), GREATEST(person1_id, person2_id));

CREATE SEQUENCE connection_seq START WITH 1 INCREMENT BY 1;
