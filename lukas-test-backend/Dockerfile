FROM eclipse-temurin:17-jre-focal

ENV TZ=Europe/Vienna

RUN apt update && apt install -y libtcnative-1 && rm -rf /var/lib/apt/lists/*

# mount container /tmp to host /var/lib/docker (or whatever is set on runtime)
# /tmp is the default working directory for Tomcat in Sprint Boot application
VOLUME /tmp

EXPOSE 8080

COPY target/lib /app/lib

ARG JAR_FILE
COPY target/${JAR_FILE} /app/lukas-test.jar

WORKDIR /app

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/lukas-test.jar"]

