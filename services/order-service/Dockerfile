FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY services/order-service/pom.xml .
RUN mvn dependency:go-offline -B
COPY services/order-service/src ./src
RUN mvn clean package -DskipTests

FROM amazoncorretto:17-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]