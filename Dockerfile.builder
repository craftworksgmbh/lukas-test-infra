FROM registry.craftworks.io/craftworks/jenkins-agents-java17-maven-node18:latest


# per component config
ARG backend=lukas-test-backend
ARG frontend=lukas-test-frontend

RUN mkdir -p /appbuilder

COPY builder /appbuilder/
RUN cd /appbuilder/ ; mvn -B dependency:go-offline || true ; mvn -B package -Ddockerfile.skip || true ; mvn -B generate-resources || true ; mvn -B compile || true
RUN cd /appbuilder/$backend ; mvn -B dependency:go-offline || true ; mvn -B package -Ddockerfile.skip || true ; mvn -B generate-resources || true ; mvn -B compile || true
RUN cd /appbuilder/$frontend ; mvn -B dependency:go-offline || true ; mvn -B package -Ddockerfile.skip || true ; mvn -B generate-resources || true ; mvn -B compile || true

RUN cd /appbuilder ; mvn -B versions:set -DnewVersion=0.0.0 || true # downloads maven version plugin
RUN cd /appbuilder ; mvn -B sonar:sonar || true # downloads sonar plugin
RUN cd /appbuilder ; mvn -B test || true # downloads testing stuff
