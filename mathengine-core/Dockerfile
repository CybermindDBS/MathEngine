FROM openjdk:18.0.1-jdk

WORKDIR /app

COPY target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod


ENTRYPOINT ["java", "-jar", "app.jar"]
