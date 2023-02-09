import { defineConfig } from 'cypress';

export default defineConfig({
  e2e: {
    baseUrl: 'http://localhost:4200',
    reporter: 'junit',
    reporterOptions: {
      mochaFile: 'cypress/results/test-results-[hash].xml',
    },
    setupNodeEvents(on, config) {
      // implement node event listeners here
    },
  },
});
