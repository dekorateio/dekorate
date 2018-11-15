FROM openjdk:8u171-alpine3.7
RUN apk --no-cache add curl
COPY target/*.jar openshift-example.jar
CMD java ${JAVA_OPTS} -jar openshift-example.jar