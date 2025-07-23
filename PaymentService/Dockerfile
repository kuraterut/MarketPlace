FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
#COPY target/classes/db/migration /app/classes/db/migration
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]