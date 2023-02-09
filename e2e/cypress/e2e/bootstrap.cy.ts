describe('bootstrap', () => {
  // ===marker:start:frontend===
  describe('frontend', () => {
    it('root element should exist', () => {
      cy.visit('/');
      cy.get('[data-testid=root]').should('exist');
    });
  });
  // ===marker:end:frontend===

  // ===marker:start:backend===
  describe('backend', () => {
    it('should call /hello-world endpoint', () => {
      cy.request({
        method: 'GET',
        url: '/api/v1/hello-world',
        failOnStatusCode: false
      }).then((response) => {
        expect(response.status).to.eq(401)
      });
    });
  });
  // ===marker:end:backend===

  // ===marker:start:python===
  // describe('python', () => {
    // TODO: Create e2e test for python
  // });
  // ===marker:end:python===
});
