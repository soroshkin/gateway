FROM adoptopenjdk:11-jre-hotspot as builder
MAINTAINER epam.com
VOLUME /tmp
EXPOSE 8099
ARG JAR_FILE=/build/libs/*.jar
COPY ${JAR_FILE} gateway.jar
ENTRYPOINT ["java","-jar","/gateway.jar"]