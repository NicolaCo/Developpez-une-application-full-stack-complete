#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."
set -a
source .env
set +a

container="${E2E_DB_CONTAINER:-back_mysql}"
database="${E2E_DB_NAME:-mdd_db_e2e}"

docker compose up -d mysql

until docker exec "$container" mysqladmin ping -h 127.0.0.1 -uroot -p"$DB_ROOT_PASSWORD" --silent >/dev/null 2>&1; do
  sleep 1
done

docker exec "$container" mysql -uroot -p"$DB_ROOT_PASSWORD" -e "
CREATE DATABASE IF NOT EXISTS \`$database\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
GRANT ALL PRIVILEGES ON \`$database\`.* TO '$DB_USER'@'%';
FLUSH PRIVILEGES;
"
