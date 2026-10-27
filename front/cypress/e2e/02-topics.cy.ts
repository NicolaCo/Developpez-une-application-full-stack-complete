import { E2E_POSTS, E2E_TOPICS } from '../support/seed';

describe('Abonnements et thématiques', () => {
  it('E2E-12 — Consulter la liste des thèmes', () => {
    cy.loginAsE2eUser('/topics');

    cy.get('[data-cy="topics-list"]').should('be.visible');
    cy.get('[data-cy="topic-card"]').should('have.length.at.least', 16);

    cy.contains('[data-cy="topic-card"]', E2E_TOPICS.subscribed[0])
      .find('[data-cy="subscribed-button"]')
      .should('contain', 'Déjà abonné');

    cy.contains('[data-cy="topic-card"]', E2E_TOPICS.subscribed[1])
      .find('[data-cy="subscribed-button"]')
      .should('contain', 'Déjà abonné');

    cy.contains('[data-cy="topic-card"]', E2E_TOPICS.notSubscribed)
      .find('[data-cy="subscribe-button"]')
      .should('contain', "S'abonner");
  });

  it('E2E-13 — S’abonner à un thème', () => {
    cy.registerUser().then((user) => {
      cy.loginWithToken(user.token, '/topics');

      cy.contains('[data-cy="topic-card"]', E2E_TOPICS.notSubscribed)
        .find('[data-cy="subscribe-button"]')
        .click();

      cy.contains('[data-cy="topic-card"]', E2E_TOPICS.notSubscribed)
        .find('[data-cy="subscribed-button"]')
        .should('exist');

      cy.visit('/profile');
      cy.contains(E2E_TOPICS.notSubscribed).should('be.visible');
    });
  });

  it('E2E-14 — Se désabonner d’un thème', () => {
    cy.loginAsE2eUser('/topics');

    cy.contains('[data-cy="topic-card"]', E2E_TOPICS.subscribed[0])
      .find('[data-cy="subscribed-button"]')
      .click();

    cy.contains('[data-cy="topic-card"]', E2E_TOPICS.subscribed[0])
      .find('[data-cy="subscribe-button"]')
      .should('exist');

    cy.visit('/profile');
    cy.contains(E2E_TOPICS.subscribed[0]).should('not.exist');
  });

  it('E2E-20 — Fil limité aux thèmes souscrits', () => {
    cy.loginAsE2eUser('/');
    cy.get('[data-cy="post-title"]').contains(E2E_POSTS.unsubscribedTopicPost).should('not.exist');

    cy.visit('/topics');
    cy.contains('[data-cy="topic-card"]', E2E_TOPICS.notSubscribed)
      .find('[data-cy="subscribe-button"]')
      .click();

    cy.visit('/');
    cy.get('[data-cy="post-title"]').contains(E2E_POSTS.unsubscribedTopicPost).should('exist');

    cy.visit('/topics');
    cy.contains('[data-cy="topic-card"]', E2E_TOPICS.notSubscribed)
      .find('[data-cy="subscribed-button"]')
      .click();

    cy.visit('/');
    cy.get('[data-cy="post-title"]').contains(E2E_POSTS.unsubscribedTopicPost).should('not.exist');
  });
});
