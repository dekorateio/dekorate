FROM openjdk:8u171-alpine3.7
RUN apk --no-cache add curl
COPY target/*.jar spring-boot-with-gradle-on-kubernetes-example.jar
CMD java ${JAVA_OPTS} -jar spring-boot-with-gradle-on-kubernetes-example.jar
