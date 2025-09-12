# Minecraft server + PaperChat Dockerfile
FROM gradle:9.0-jdk21 AS builder

# Set up working directory
WORKDIR /app

# Copy project files
COPY . .

# Configure and run builder
RUN gradle wrapper --gradle-version 9.0
RUN ./gradlew build

# Production stage with Minecraft server
FROM itzg/minecraft-server:java21

# Set environment variables for the server
ENV TYPE=PAPER
ENV EULA=TRUE

# Copy the built plugin
COPY --from=builder /app/build/libs/paperchat-*.jar /plugins/

# Expose the default Minecraft port
EXPOSE 25565
