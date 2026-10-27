--liquibase formatted sql

--changeset e2e:900-seed-e2e context:e2e runAlways:true

-- Utilisateurs (mot de passe : E2ePassword1!)
INSERT INTO users (email, username, password)
SELECT 'e2e@mdd.test', 'e2e_user',
       '$2a$10$j4VoBv6PRuD.Bh4xUdKzcuaC/TyFxlJZIOhIGkRqb6hmC2cCJ62NK'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'e2e@mdd.test');

INSERT INTO users (email, username, password)
SELECT 'e2e.author@mdd.test', 'e2e_author',
       '$2a$10$j4VoBv6PRuD.Bh4xUdKzcuaC/TyFxlJZIOhIGkRqb6hmC2cCJ62NK'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'e2e.author@mdd.test');

-- Abonnements : Angular + Spring Boot, mais pas Tests (utile pour E2E-20)
INSERT INTO subscriptions (user_id, topic_id)
SELECT u.id, t.id
FROM users u
JOIN topics t ON t.title IN ('Angular', 'Spring Boot')
WHERE u.email = 'e2e@mdd.test'
  AND NOT EXISTS (
      SELECT 1 FROM subscriptions s
      WHERE s.user_id = u.id AND s.topic_id = t.id
  );

-- Articles, avec des dates différentes pour les tris
INSERT INTO posts (title, content, date, topic_id, user_id)
SELECT 'Découvrir Angular', 'Un premier article sur Angular.',
       '2026-01-10 10:00:00', t.id, u.id
FROM topics t JOIN users u ON u.email = 'e2e@mdd.test'
WHERE t.title = 'Angular'
  AND NOT EXISTS (SELECT 1 FROM posts p WHERE p.title = 'Découvrir Angular');

INSERT INTO posts (title, content, date, topic_id, user_id)
SELECT 'Les signals en Angular', 'Un article plus récent sur Angular.',
       '2026-01-12 10:00:00', t.id, u.id
FROM topics t JOIN users u ON u.email = 'e2e.author@mdd.test'
WHERE t.title = 'Angular'
  AND NOT EXISTS (SELECT 1 FROM posts p WHERE p.title = 'Les signals en Angular');

INSERT INTO posts (title, content, date, topic_id, user_id)
SELECT 'Démarrer avec Spring Boot', 'Un article sur le backend Spring Boot.',
       '2026-01-11 10:00:00', t.id, u.id
FROM topics t JOIN users u ON u.email = 'e2e@mdd.test'
WHERE t.title = 'Spring Boot'
  AND NOT EXISTS (SELECT 1 FROM posts p WHERE p.title = 'Démarrer avec Spring Boot');

-- Article dans un thème non souscrit : ne doit pas apparaître dans le feed
INSERT INTO posts (title, content, date, topic_id, user_id)
SELECT 'Tester son application', 'Un article sur les tests.',
       '2026-01-13 10:00:00', t.id, u.id
FROM topics t JOIN users u ON u.email = 'e2e.author@mdd.test'
WHERE t.title = 'Tests'
  AND NOT EXISTS (SELECT 1 FROM posts p WHERE p.title = 'Tester son application');

-- Commentaire pour E2E-18
INSERT INTO comments (content, date, post_id, user_id)
SELECT 'Très bon article, merci !', '2026-01-10 11:00:00', p.id, u.id
FROM posts p JOIN users u ON u.email = 'e2e.author@mdd.test'
WHERE p.title = 'Découvrir Angular'
  AND NOT EXISTS (
      SELECT 1 FROM comments c
      WHERE c.post_id = p.id AND c.content = 'Très bon article, merci !'
  );

--rollback DELETE FROM comments WHERE user_id IN (SELECT id FROM users WHERE email LIKE '%@mdd.test');
--rollback DELETE FROM posts WHERE user_id IN (SELECT id FROM users WHERE email LIKE '%@mdd.test');
--rollback DELETE FROM subscriptions WHERE user_id IN (SELECT id FROM users WHERE email LIKE '%@mdd.test');
--rollback DELETE FROM users WHERE email LIKE '%@mdd.test';
