# __nameDisplay__ (__nameKebab__)

Staging: [__nameKebab__-staging.craftworks.io](https://__nameKebab__-staging.k8s1.craftworks.io/)  
Dev: [__nameKebab__-dev.craftworks.io](https://__nameKebab__-dev.k8s1.craftworks.io)

SonarQube: [sonarqube.craftworks.io/dashboard?id=__nameKebab__](https://sonarqube.craftworks.io/dashboard?id=__nameKebab__)

## Components

* ===marker:start:backend===
* __nameDisplay__ Backend ([__nameKebab__-backend](./__nameKebab__-backend))
* ===marker:end:backend===
* ===marker:start:frontend===
* __nameDisplay__ Frontend ([__nameKebab__-frontend](./__nameKebab__-frontend))
* ===marker:end:frontend===
* ===marker:start:python===
* __nameDisplay__ Python ([__nameKebab__-python](./__nameKebab__-python))
* ===marker:end:python===

See [craftworks-template/README.md#how-tos](https://github.com/craftworksgmbh/craftworks-template#how-tos) on how to add/remove a component

## Usage

### Build

*Prerequisites*

* Maven  
* Node.js (Run `nvm use`)  
* ===marker:start:python===
* Python  
* ===marker:end:python===

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
