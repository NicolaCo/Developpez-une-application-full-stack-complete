import { E2E_USERS } from '../support/seed';

const subscriber = E2E_USERS.subscriber;

function uniqueSuffix(): string {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
}

describe('Authentification et accès', () => {
  it('E2E-01 — Accès aux formulaires depuis l’accueil non connecté', () => {
    cy.visit('/');
    cy.get('[data-cy="login-link"]').click();
    cy.url().should('include', '/login');
    cy.get('[data-cy="login-form"]').should('be.visible');
    cy.get('[data-cy="login-email"]').should('be.visible');
    cy.get('[data-cy="login-password"]').should('be.visible');

    cy.visit('/');
    cy.get('[data-cy="signup-link"]').click();
    cy.url().should('include', '/signup');
    cy.get('[data-cy="signup-form"]').should('be.visible');
    cy.get('[data-cy="signup-email"]').should('be.visible');
    cy.get('[data-cy="signup-username"]').should('be.visible');
    cy.get('[data-cy="signup-password"]').should('be.visible');

    cy.get('[data-cy="signup-home-link"]').should('be.visible');
  });

  it('E2E-02 — Inscription valide', () => {
    const suffix = uniqueSuffix();

    cy.visit('/signup');
    cy.get('[data-cy="signup-email"]').type(`signup-${suffix}@mdd.test`);
    cy.get('[data-cy="signup-username"]').type(`signup_${suffix.replace(/[^a-z0-9]/gi, '_')}`);
    cy.get('[data-cy="signup-password"]').type('E2ePassword1!');
    cy.get('[data-cy="signup-submit"]').click();

    cy.url().should('eq', 'http://localhost:4200/');
    cy.get('[data-cy="feed"]').should('be.visible');

    cy.visit('/profile');
    cy.get('[data-cy="profile"]').should('be.visible');
  });

  it('E2E-03 — Inscription refusée si email ou nom d’utilisateur déjà utilisé', () => {
    cy.visit('/signup');
    cy.get('[data-cy="signup-email"]').type(subscriber.email);
    cy.get('[data-cy="signup-username"]').type(`other_${uniqueSuffix()}`);
    cy.get('[data-cy="signup-password"]').type(subscriber.password);
    cy.get('[data-cy="signup-submit"]').click();

    cy.get('[data-cy="signup-error"]').should('be.visible');
    cy.url().should('include', '/signup');

    cy.visit('/signup');
    cy.get('[data-cy="signup-email"]').type(`other-${uniqueSuffix()}@mdd.test`);
    cy.get('[data-cy="signup-username"]').type(subscriber.username);
    cy.get('[data-cy="signup-password"]').type(subscriber.password);
    cy.get('[data-cy="signup-submit"]').click();

    cy.get('[data-cy="signup-error"]').should('be.visible');
    cy.url().should('include', '/signup');
  });

  const invalidPasswords = ['Ab1!', 'Abcdefg!', 'ABCDEF1!', 'abcdef1!', 'Abcdef12'];

  invalidPasswords.forEach((password, index) => {
    it(`E2E-04 — Validation du mot de passe à l’inscription (cas ${index + 1})`, () => {
      cy.visit('/signup');
      cy.get('[data-cy="signup-email"]').type(`password-${uniqueSuffix()}@mdd.test`);
      cy.get('[data-cy="signup-username"]').type(`password_${uniqueSuffix()}`);
      cy.get('[data-cy="signup-password"]').type(password);
      cy.get('[data-cy="signup-submit"]').click();

      cy.get('[data-cy="signup-error"]').should('be.visible');
      cy.url().should('include', '/signup');
    });
  });

  it('E2E-05 — Connexion par email', () => {
    cy.loginViaUi(subscriber.email, subscriber.password);

    cy.url().should('eq', 'http://localhost:4200/');
    cy.get('[data-cy="feed"]').should('be.visible');
    cy.get('[data-cy="navbar"]').should('be.visible');
  });

  it('E2E-06 — Connexion par nom d’utilisateur', () => {
    cy.loginViaUi(subscriber.username, subscriber.password);

    cy.url().should('eq', 'http://localhost:4200/');
    cy.get('[data-cy="feed"]').should('be.visible');
  });

  it('E2E-07 — Connexion refusée avec identifiants invalides', () => {
    cy.visit('/login');
    cy.get('[data-cy="login-email"]').type(subscriber.email);
    cy.get('[data-cy="login-password"]').type('WrongPassword1!');
    cy.get('[data-cy="login-submit"]').click();
    cy.get('[data-cy="login-error"]').should('be.visible');

    cy.get('[data-cy="login-email"]').clear().type(`unknown-${uniqueSuffix()}@mdd.test`);
    cy.get('[data-cy="login-password"]').clear().type(subscriber.password);
    cy.get('[data-cy="login-submit"]').click();
    cy.get('[data-cy="login-error"]').should('be.visible');

    cy.window().then((win) => {
      expect(win.localStorage.getItem('mdd_token')).to.be.null;
    });
  });

  it('E2E-08 — Persistance de la session entre les sessions', () => {
    cy.loginAsE2eUser();

    cy.reload();
    cy.get('[data-cy="navbar"]').should('be.visible');
    cy.get('[data-cy="feed"]').should('be.visible');

    cy.visit('/topics');
    cy.get('[data-cy="topics-list"]').should('be.visible');
  });

  it('E2E-11 — Se déconnecter', () => {
    cy.loginAsE2eUser();

    cy.get('[data-cy="logout"]').click();
    cy.url().should('eq', 'http://localhost:4200/');
    cy.get('[data-cy="login-link"]').should('be.visible');

    cy.visit('/posts');
    cy.url().should('include', '/login');
  });

  it('E2E-22 — Accès aux pages protégées sans session', () => {
    ['/topics', '/posts', '/posts/new', '/posts/1'].forEach((path) => {
      cy.visit(path);
      cy.url().should('include', '/login');
      cy.url().should('include', 'returnUrl');
    });

    cy.visit('/profile');
    cy.url().should('include', '/login');
  });

  it('E2E-22 — Retour à la page demandée après connexion', () => {
    cy.visit('/posts');
    cy.url().should('include', '/login?returnUrl=%2Fposts');

    cy.get('[data-cy="login-email"]').type(subscriber.email);
    cy.get('[data-cy="login-password"]').type(subscriber.password);
    cy.get('[data-cy="login-submit"]').click();

    cy.url().should('eq', 'http://localhost:4200/posts');
    cy.get('[data-cy="posts-list"]').should('be.visible');
  });
});
