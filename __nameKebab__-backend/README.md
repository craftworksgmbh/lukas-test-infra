# __nameDisplay__ Backend

## Develop

### Spring Profiles

We make use of various Spring profiles to handle permission level and to compartmentalize aspects of the application.

Following spring profiles are defined:

- `noauth` Indicates that all authentication is disabled for simplicity. Should be used for local development only!
- `e2e` Used for end-to-end testing.
- `dev` For use on the dev staging environment along with the profile `testdata` if test data needs generating. This one enables swagger.
- `testdata` Used to signify generation of test data. Usually, via test generator classes that import data from json files usually located in `src/main/resources/testdata`.
    - Used in conjunction with `dev` profile when deploying to dev staging environment.
    - Used in conjunction with `test` profile as the base unit test context.
- `test` Main test context for unit tests. Should be used for local development only!
- `prod` Used for a production-ready environment.