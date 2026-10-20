--liquibase formatted sql

--changeset dev:002-create-topics-table
CREATE TABLE topics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) UNIQUE NOT NULL,
    description TEXT NOT NULL
);

--rollback DROP TABLE topics;

--changeset dev:003-seed-topics
INSERT INTO topics (title, description) VALUES
    ('Angular', 'TypeScript front-end framework for building modern web applications.'),
    ('Spring Boot', 'Java framework for building REST APIs and server applications.'),
    ('Tests', 'Unit tests and integration tests for reliable code.'),
    ('Architecture', 'Design and organization of an application''s code.'),
    ('DevOps', 'Continuous integration and deployment, tooling and industrialization.'),
    ('Java', 'Object-oriented programming language for the JVM platform.'),
    ('TypeScript', 'Typed superset of JavaScript for the front end.'),
    ('REST API', 'HTTP API design and REST best practices.'),
    ('Security', 'Authentication, authorization and security best practices.'),
    ('Database', 'Data modeling, SQL queries and persistence.'),
    ('Docker', 'Containerization and application deployment.'),
    ('Git', 'Version control and collaborative work.'),
    ('Kotlin', 'Modern JVM language, interoperable with Java.'),
    ('Kubernetes', 'Container orchestration and large-scale deployment.'),
    ('CI/CD', 'Continuous integration and deployment pipelines.'),
    ('Cloud', 'Hosting and cloud services for applications.');

--rollback DELETE FROM topics;

--changeset dev:004-create-subscriptions-table
CREATE TABLE subscriptions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL,
    CONSTRAINT uq_subscriptions_user_topic UNIQUE (user_id, topic_id),
    CONSTRAINT fk_subscriptions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_subscriptions_topic FOREIGN KEY (topic_id) REFERENCES topics (id) ON DELETE CASCADE
);

--rollback DROP TABLE subscriptions;
