import { E2E_POSTS, E2E_TOPICS, E2E_USERS } from '../support/seed';

interface ApiPost {
  title: string;
}

describe('Fil d’actualité et articles', () => {
  it('E2E-15 — Fil d’actualité par défaut', () => {
    cy.loginAsE2eUser('/');

    cy.token().then((token) => {
      cy.request({
        url: `${Cypress.expose('apiUrl')}/posts?order=desc`,
        headers: { Authorization: `Bearer ${token}` }
      }).then((response) => {
        cy.get('[data-cy="post-title"]').should(($titles) => {
          const displayed = $titles.toArray().map((element) => element.textContent?.trim());
          const expected = (response.body as ApiPost[]).map((post) => post.title);
          expect(displayed).to.deep.eq(expected);
        });
      });
    });
  });

  it('E2E-16 — Trier le fil d’actualité', () => {
    cy.loginAsE2eUser('/');

    cy.get('[data-cy="post-title"]').then(($titles) => {
      const descending = $titles.toArray().map((element) => element.textContent?.trim());

      cy.get('[data-cy="sort-toggle"]').click();

      cy.get('[data-cy="post-title"]').should(($sorted) => {
        const ascending = $sorted.toArray().map((element) => element.textContent?.trim());
        expect(ascending).to.deep.eq([...descending].reverse());
      });
    });
  });

  it('E2E-17 — Ajouter un article', () => {
    const title = `Article Cypress ${Date.now()}`;
    const content = 'Contenu créé par le test end-to-end.';

    cy.intercept('GET', '**/api/topics').as('topics');
    cy.loginAsE2eUser('/posts/new');
    cy.wait('@topics');

    cy.get('[data-cy="create-post-topic"]').click();
    cy.get('mat-option').should('have.length.at.least', 1);
    cy.contains('mat-option', E2E_TOPICS.subscribed[0]).click();
    cy.get('[data-cy="create-post-title"]').type(title);
    cy.get('[data-cy="create-post-content"]').type(content);
    cy.get('[data-cy="create-post-submit"]').click();

    cy.url().should('match', /\/posts\/\d+$/);
    cy.get('[data-cy="post-title"]').should('contain', title);
    cy.get('[data-cy="post-content"]').should('contain', content);
    cy.get('[data-cy="post-meta"]').should('contain', E2E_USERS.subscriber.username);

    cy.visit('/');
    cy.get('[data-cy="post-title"]').first().should('contain', title);
  });

  it('E2E-18 — Consulter un article', () => {
    cy.loginAsE2eUser('/');

    cy.contains('[data-cy="post-card"]', E2E_POSTS.withComment)
      .find('[data-cy="detail-button"]')
      .click();

    cy.get('[data-cy="post-title"]').should('contain', E2E_POSTS.withComment);
    cy.get('[data-cy="post-content"]').should('not.be.empty');
    cy.get('[data-cy="post-meta"]').should('contain', E2E_USERS.subscriber.username);
    cy.get('[data-cy="comment"]').should('have.length.at.least', 1);
    cy.get('[data-cy="comment-content"]').should('contain', 'Très bon article, merci !');
    cy.get('[data-cy="post-topic"]').should('contain', E2E_TOPICS.subscribed[0]);
  });

  it('E2E-19 — Ajouter un commentaire', () => {
    const content = `Commentaire Cypress ${Date.now()}`;

    cy.loginAsE2eUser('/');
    cy.contains('[data-cy="post-card"]', E2E_POSTS.withComment)
      .find('[data-cy="detail-button"]')
      .click();

    cy.get('[data-cy="comment-input"]').type(content);
    cy.get('[data-cy="comment-submit"]').click();

    cy.get('[data-cy="comment"]').should(($comments) => {
      const contents = $comments.toArray().map((comment) => comment.textContent ?? '');
      expect(contents.some((text) => text.includes(content))).to.equal(true);
    });

    cy.get('[data-cy="comment"]').last().should('contain', E2E_USERS.subscriber.username);
    cy.get('[data-cy="comment"] [data-cy="comment-input"]').should('not.exist');
    cy.get('[data-cy="comment"] [data-cy="comment-submit"]').should('not.exist');
  });
});
