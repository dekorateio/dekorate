FROM openjdk:8u171-alpine3.7
RUN apk --no-cache add curl
COPY target/*.jar kubernetes-with-hostaliases-example.jar
CMD java ${JAVA_OPTS} -jar kubernetes-with-hostaliases-example.jar
