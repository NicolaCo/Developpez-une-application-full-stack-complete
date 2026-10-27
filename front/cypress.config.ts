import { execFileSync } from 'node:child_process';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { defineConfig } from 'cypress';

const database = {
  container: process.env['E2E_DB_CONTAINER'] ?? 'back_mysql',
  name: process.env['E2E_DB_NAME'] ?? 'mdd_db_e2e',
  user: process.env['E2E_DB_USER'] ?? 'user_one',
  password: process.env['E2E_DB_PASSWORD'] ?? 'password_usr_one'
};

const truncateSql = `
SET FOREIGN_KEY_CHECKS=0;
TRUNCATE TABLE comments;
TRUNCATE TABLE posts;
TRUNCATE TABLE subscriptions;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS=1;
`;

const seedSql = readFileSync(
  resolve(__dirname, '../back/src/main/resources/db/changelog/sql/900-e2e-seed.sql'),
  'utf8'
).replace(/^--.*$/gm, '');

function resetDatabase(): void {
  execFileSync(
    'docker',
    [
      'exec',
      '-i',
      '-e',
      `MYSQL_PWD=${database.password}`,
      database.container,
      'mysql',
      '--default-character-set=utf8mb4',
      `-u${database.user}`,
      database.name
    ],
    {
      input: `${truncateSql}\n${seedSql}`,
      stdio: ['pipe', 'inherit', 'inherit']
    }
  );
}

export default defineConfig({
  e2e: {
    baseUrl: 'http://localhost:4200',
    specPattern: 'cypress/e2e/**/*.cy.ts',
    supportFile: 'cypress/support/e2e.ts',
    viewportWidth: 1280,
    viewportHeight: 800,
    defaultCommandTimeout: 8000,
    responseTimeout: 15000,
    video: false,
    screenshotOnRunFailure: false,
    retries: {
      runMode: 1,
      openMode: 0
    },
    expose: {
      apiUrl: 'http://localhost:8080/api'
    },
    reporter: 'mochawesome',
    reporterOptions: {
      reportDir: 'cypress/reports',
      overwrite: false,
      html: true,
      json: true,
      timestamp: 'yyyy_mm_dd_HH-MM-ss'
    },
    setupNodeEvents(on) {
      on('task', {
        resetDb() {
          resetDatabase();
          return null;
        }
      });
    }
  }
});
