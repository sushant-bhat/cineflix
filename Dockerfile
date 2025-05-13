FROM openjdk:21-jdk-slim
LABEL authors="Anthat Studios"
RUN apt-get update && \
    apt-get install -y ffmpeg && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
ADD target/cineflix.jar cineflix.jar
RUN mkdir -p /app/data
ENTRYPOINT ["java", "-jar", "/cineflix.jar"]