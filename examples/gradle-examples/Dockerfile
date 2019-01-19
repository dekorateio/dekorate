FROM openjdk:8-alpine

ENV path /opt
ENV TZ "Europe/Berlin"

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY build/libs/kubernetes-java-annotations-0.0.1-SNAPSHOT.jar /opt/


CMD java -jar /opt/kubernetes-java-annotations-0.0.1-SNAPSHOT.jar
