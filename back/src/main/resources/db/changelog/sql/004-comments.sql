--liquibase formatted sql

--changeset dev:005-create-comments-table
CREATE TABLE comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content TEXT NOT NULL,
    date DATETIME(6) NOT NULL,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts(id),
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_comments_post_id ON comments(post_id);

CREATE INDEX idx_comments_user_id ON comments(user_id);

--rollback DROP TABLE comments;
