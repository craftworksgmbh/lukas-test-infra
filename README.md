# Lukas AG test (lukas-test)

Staging: [lukas-test-staging.craftworks.io](https://lukas-test-staging.k8s1.craftworks.io/)  
Dev: [lukas-test-dev.craftworks.io](https://lukas-test-dev.k8s1.craftworks.io)

SonarQube: [sonarqube.craftworks.io/dashboard?id=lukas-test](https://sonarqube.craftworks.io/dashboard?id=lukas-test)

## Components

* Lukas AG test Backend ([lukas-test-backend](./lukas-test-backend))
* Lukas AG test Frontend ([lukas-test-frontend](./lukas-test-frontend))

See [craftworks-template/README.md#how-tos](https://github.com/craftworksgmbh/craftworks-template#how-tos) on how to add/remove a component

## Usage

### Build

*Prerequisites*

* Maven  
* Node.js (Run `nvm use`)  

We use Maven to build the application. Check [Maven Build Lifecycle](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html).

```
# Build application
# add -DskipTests to skip tests
mvn package 

# build docker images tagged latest
mvn dockerfile:build dockerfile:tag
```

Other options include:
```
# build in one command
mvn package dockerfile:build dockerfile:tag

# build component separately via Maven profile
mvn -P [profile] package dockerfile:build dockerfile:tag
```

### Run

Use docker-compose to start up the application.  
Make sure you have built the application and docker images beforehand.

```
docker-compose up -d
```

### Deploy

Check [./deploy](./deploy)

### E2E Tests

Run E2E Tests via

```
sh ./scripts/run-e2e.sh
```

Check [./e2e](./e2e)

## CI / CD

Check [./Jenkinsfile](./Jenkinsfile)

## License

[LICENSE](./LICENSE.md)
