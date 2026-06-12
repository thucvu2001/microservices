FROM openjdk:17

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} job-scheduling.jar

ENTRYPOINT ["java", "-jar", "job-scheduling.jar"]

EXPOSE 8089