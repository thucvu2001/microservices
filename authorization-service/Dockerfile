FROM openjdk:17

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} authorization-service.jar

ENTRYPOINT ["java", "-jar", "authorization-service.jar"]

EXPOSE 8090