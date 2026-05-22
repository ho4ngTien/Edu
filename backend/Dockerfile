# Stage 1: Build the application
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies first to cache this layer
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application on lightweight OpenJDK 25 runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/ide-0.0.1-SNAPSHOT.jar app.jar

# Install standard JDK 25 compiler tools so dynamic javac/java processes work in the container!
# By installing the JDK, dynamic ToolProvider.getSystemJavaCompiler() will run perfectly in Docker!
# Let's switch from JRE to JDK in stage 2 to support dynamic compiling in containerized environments.
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/ide-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
