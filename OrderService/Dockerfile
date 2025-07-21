FROM eclipse-temurin:17-jdk-jammy
#TODO Переключить на JRE
WORKDIR /app
#COPY target/classes/db/migration /app/classes/db/migration
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]