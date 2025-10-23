# Start with a base Java image
FROM openjdk:21-jdk-slim

# Set the working directory
WORKDIR /app

# Copy Maven wrapper files
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy application source code
COPY src ./src

# Build the application JAR
RUN ./mvnw package -B -DskipTests

# Tell Docker the application listens on port 8081
EXPOSE 8081

# Command to run when the container starts
# Make sure the JAR filename matches
ENTRYPOINT ["java", "-jar", "target/analyzer-service-0.0.1-SNAPSHOT.jar"]