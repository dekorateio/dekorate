FROM openjdk:8-alpine

ENV path /opt
ENV TZ "Europe/Berlin"

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY build/libs/*.jar kubernetes-example.jar
CMD java ${JAVA_OPTS} -jar kubernetes-example.jar
