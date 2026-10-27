export interface E2eCredentials {
  email: string;
  username: string;
  password: string;
}

export interface RegisteredUser extends E2eCredentials {
  token: string;
}

const DEFAULT_EMAIL = 'e2e@mdd.test';
const DEFAULT_PASSWORD = 'E2ePassword1!';

function apiUrl(path: string): string {
  return `${Cypress.expose('apiUrl')}${path}`;
}

declare global {
  // eslint-disable-next-line @typescript-eslint/no-namespace
  namespace Cypress {
    interface Chainable {
      login(email?: string, password?: string, path?: string): Chainable<void>;
      loginAsE2eUser(path?: string): Chainable<void>;
      loginViaUi(email?: string, password?: string): Chainable<void>;
      loginWithToken(token: string, path?: string): Chainable<void>;
      logout(): Chainable<void>;
      registerUser(credentials?: Partial<E2eCredentials>): Chainable<RegisteredUser>;
      registerAndLogin(credentials?: Partial<E2eCredentials>, path?: string): Chainable<RegisteredUser>;
      token(): Chainable<string>;
    }
  }
}

Cypress.Commands.add('login', (email = DEFAULT_EMAIL, password = DEFAULT_PASSWORD, path = '/') => {
  cy.request({
    method: 'POST',
    url: apiUrl('/auth/login'),
    body: { email, password }
  }).then((response) => {
    cy.loginWithToken(response.body.token as string, path);
  });
});

Cypress.Commands.add('loginAsE2eUser', (path = '/') => {
  cy.login(DEFAULT_EMAIL, DEFAULT_PASSWORD, path);
});

Cypress.Commands.add('loginViaUi', (email = DEFAULT_EMAIL, password = DEFAULT_PASSWORD) => {
  cy.visit('/login');
  cy.get('[data-cy="login-email"]').type(email);
  cy.get('[data-cy="login-password"]').type(password);
  cy.get('[data-cy="login-submit"]').click();
});

Cypress.Commands.add('loginWithToken', (token: string, path = '/') => {
  cy.visit(path, {
    onBeforeLoad(win) {
      win.localStorage.setItem('mdd_token', token);
    }
  });
});

Cypress.Commands.add('logout', () => {
  cy.window().then((win) => {
    win.localStorage.removeItem('mdd_token');
  });
});

Cypress.Commands.add('registerUser', (credentials = {}) => {
  const stamp = `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
  const email = credentials.email ?? `cypress-${stamp}@mdd.test`;
  const username = credentials.username ?? `cypress_${stamp.replace(/[^a-z0-9]/gi, '_')}`;
  const password = credentials.password ?? DEFAULT_PASSWORD;

  return cy.request({
    method: 'POST',
    url: apiUrl('/auth/register'),
    body: { email, username, password }
  }).then((response) => ({
    email,
    username,
    password,
    token: response.body.token as string
  }));
});

Cypress.Commands.add('registerAndLogin', (credentials = {}, path = '/') => {
  return cy.registerUser(credentials).then((user) => {
    cy.loginWithToken(user.token, path);
    return cy.wrap(user);
  });
});

Cypress.Commands.add('token', () => {
  return cy.window().then((win) => {
    const token = win.localStorage.getItem('mdd_token');
    if (!token) {
      throw new Error('Aucun token mdd_token trouvé dans le localStorage');
    }
    return token;
  });
});
