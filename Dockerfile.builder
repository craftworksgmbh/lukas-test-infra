FROM registry.craftworks.io/craftworks/jenkins-agents-java17-maven-node18:latest

# ===marker:start:python===
# Install python, pip & cli tools
# TODO: Move to base image
RUN apt-get update && \
    apt-get -y install python3-pip software-properties-common && \
    add-apt-repository ppa:deadsnakes/ppa && \
    apt-get update && \
    apt-get -y install \
        python3 \
        python3-dev && \
    rm -rf /var/lib/apt/lists/* && \
    python3 -m pip install --upgrade pip

# Install python packages necessary for builder only
# TODO: Evaluate whether we should just install all python requirements here
RUN pip3 install tbump
# ===marker:end:python===

# per component config
# ===marker:start:backend===
ARG backend=__nameKebab__-backend
# ===marker:end:backend===
# ===marker:start:frontend===
ARG frontend=__nameKebab__-frontend
# ===marker:end:frontend===
# ===marker:start:python===
ARG python=__nameKebab__-python
# ===marker:end:python===

RUN mkdir -p /appbuilder

COPY builder /appbuilder/
RUN cd /appbuilder/ ; mvn -B dependency:go-offline || true ; mvn -B package -Ddockerfile.skip || true ; mvn -B generate-resources || true ; mvn -B compile || true
# ===marker:start:backend===
RUN cd /appbuilder/$backend ; mvn -B dependency:go-offline || true ; mvn -B package -Ddockerfile.skip || true ; mvn -B generate-resources || true ; mvn -B compile || true
# ===marker:end:backend===
# ===marker:start:frontend===
RUN cd /appbuilder/$frontend ; mvn -B dependency:go-offline || true ; mvn -B package -Ddockerfile.skip || true ; mvn -B generate-resources || true ; mvn -B compile || true
# ===marker:end:frontend===
# ===marker:start:python===
RUN cd /appbuilder/$python ; mvn -B dependency:go-offline || true ; mvn -B package -Ddockerfile.skip || true ; mvn -B generate-resources || true ; mvn -B compile || true
# ===marker:end:python===

RUN cd /appbuilder ; mvn -B versions:set -DnewVersion=0.0.0 || true # downloads maven version plugin
RUN cd /appbuilder ; mvn -B sonar:sonar || true # downloads sonar plugin
RUN cd /appbuilder ; mvn -B test || true # downloads testing stuff
