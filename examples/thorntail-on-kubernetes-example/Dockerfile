FROM openjdk:8u171-alpine3.7
RUN apk --no-cache add curl
COPY target/*-thorntail.jar thorntail-on-kubernetes-example.jar
CMD java ${JAVA_OPTS} -jar thorntail-on-kubernetes-example.jar
