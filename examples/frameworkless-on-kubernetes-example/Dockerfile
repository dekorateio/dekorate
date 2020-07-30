FROM openjdk:8-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} frameworkless-on-kubernetes-example.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/frameworkless-on-kubernetes-example.jar"]
