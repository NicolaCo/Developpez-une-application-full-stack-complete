#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."
set -a
source .env
set +a

container="${E2E_DB_CONTAINER:-back_mysql}"
database="${E2E_DB_NAME:-mdd_db_e2e}"
user="${E2E_DB_USER:-user_one}"
password="${E2E_DB_PASSWORD:-password_usr_one}"
seed_file="src/main/resources/db/changelog/sql/900-e2e-seed.sql"

if [ ! -f "$seed_file" ]; then
  echo "Erreur : fichier seed introuvable $seed_file" >&2
  exit 1
fi

echo "Réinitialisation de la base E2E '$database' sur le conteneur '$container'..."

truncate_sql="$(cat <<'SQL'
SET FOREIGN_KEY_CHECKS=0;
TRUNCATE TABLE comments;
TRUNCATE TABLE posts;
TRUNCATE TABLE subscriptions;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS=1;
SQL
)"

seed_sql="$(grep -v '^--' "$seed_file")"

printf "%s\n\n%s\n" "$truncate_sql" "$seed_sql" | docker exec -i -e "MYSQL_PWD=$password" "$container" mysql \
  --default-character-set=utf8mb4 \
  -u"$user" \
  "$database"

echo "Base E2E réinitialisée avec succès."
