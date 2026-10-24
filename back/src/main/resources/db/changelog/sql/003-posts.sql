--liquibase formatted sql

--changeset dev:004-create-posts-table
CREATE TABLE posts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    date DATETIME(6) NOT NULL,
    topic_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_posts_topic FOREIGN KEY (topic_id) REFERENCES topics(id),
    CONSTRAINT fk_posts_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_posts_topic_id ON posts(topic_id);

CREATE INDEX idx_posts_user_id ON posts(user_id);

--rollback DROP TABLE posts;
