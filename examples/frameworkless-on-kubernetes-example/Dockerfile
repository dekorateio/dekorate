FROM adoptopenjdk/openjdk8

ARG JAR_FILE=target/*.jar
RUN groupadd -r appgroup && useradd -r -g appgroup -s /sbin/nologin appuser
WORKDIR /app
COPY ${JAR_FILE} /app/frameworkless-on-kubernetes-example.jar

RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080
ENTRYPOINT ["java","-jar","frameworkless-on-kubernetes-example.jar"]
