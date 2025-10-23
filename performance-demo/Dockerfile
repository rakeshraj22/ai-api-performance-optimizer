# Start with a base Java image (using Java 17 like your project)
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy Maven wrapper files first (efficient caching)
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download dependencies using the Maven wrapper
# This layer is cached if pom.xml doesn't change
RUN ./mvnw dependency:go-offline -B

# Copy the rest of your application source code
COPY src ./src

# Build the application JAR file using the Maven wrapper
# -B runs in batch mode, -DskipTests skips running tests
RUN ./mvnw package -B -DskipTests

# Tell Docker the application listens on port 8080
EXPOSE 8080

# The command to run when the container starts
# Make sure the JAR filename matches what's built in the 'target' directory
ENTRYPOINT ["java", "-jar", "target/performance-demo-0.0.1-SNAPSHOT.jar"]