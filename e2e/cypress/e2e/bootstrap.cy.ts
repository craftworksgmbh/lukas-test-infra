describe('bootstrap', () => {
  describe('frontend', () => {
    it('root element should exist', () => {
      cy.visit('/');
      cy.get('[data-testid=root]').should('exist');
    });
  });

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

});
