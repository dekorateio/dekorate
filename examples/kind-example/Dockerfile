FROM openjdk:8u171-alpine3.7
RUN apk --no-cache add curl
COPY target/*.jar kind-example.jar
CMD java ${JAVA_OPTS} -jar kind-example.jar
