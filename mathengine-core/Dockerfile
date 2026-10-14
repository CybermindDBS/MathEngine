FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod


ENTRYPOINT ["java", "-jar", "app.jar"]
