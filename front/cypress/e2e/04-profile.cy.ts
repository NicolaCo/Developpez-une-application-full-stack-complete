import { E2E_TOPICS, E2E_USERS } from '../support/seed';

describe('Profil utilisateur', () => {
  it('E2E-09 — Consulter son profil', () => {
    cy.loginAsE2eUser('/profile');

    cy.get('[data-cy="profile"]').should('be.visible');
    cy.contains(E2E_USERS.subscriber.email).should('be.visible');
    cy.contains(E2E_USERS.subscriber.username).should('be.visible');
    cy.contains(E2E_TOPICS.subscribed[0]).should('be.visible');
    cy.contains(E2E_TOPICS.subscribed[1]).should('be.visible');
  });

  it('E2E-10 — Modifier son profil', () => {
    const newEmail = `profil-${Date.now()}@mdd.test`;
    const newUsername = `profil_${Date.now()}`;
    const newPassword = 'NewPassword1!';

    cy.loginAsE2eUser('/profile');

    cy.get('[data-cy="profile-email"]').clear().type(newEmail);
    cy.get('[data-cy="profile-username"]').clear().type(newUsername);
    cy.get('[data-cy="profile-password"]').clear().type(newPassword);
    cy.get('[data-cy="profile-save"]').click();

    cy.get('[data-cy="profile"]').should('contain', newEmail);
    cy.get('[data-cy="profile"]').should('contain', newUsername);

    cy.get('[data-cy="logout"]').click();
    cy.loginViaUi(newUsername, newPassword);

    cy.url().should('eq', 'http://localhost:4200/');
    cy.get('[data-cy="feed"]').should('be.visible');
  });
});
