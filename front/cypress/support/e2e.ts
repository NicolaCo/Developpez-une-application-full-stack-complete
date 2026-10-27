import './commands';

beforeEach(() => {
  cy.task('resetDb');
  Cypress.session.clearAllSavedSessions();
  cy.clearLocalStorage();
  cy.clearCookies();
});
