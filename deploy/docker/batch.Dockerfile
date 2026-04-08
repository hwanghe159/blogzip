FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace/backend

COPY backend/ ./

RUN chmod +x gradlew && ./gradlew --no-daemon batch:bootJar

FROM eclipse-temurin:21-jre

ENV JAVA_TOOL_OPTIONS="-Xms256M -Xmx256M -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/heapdumps -XX:+ExitOnOutOfMemoryError"

WORKDIR /app

COPY --from=build /workspace/backend/batch/build/libs/*.jar /app/app.jar

RUN mkdir -p /app/heapdumps

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
CMD ["--spring.profiles.active=prod", "--server.port=8081", "--spring.batch.job.name=fetch-new-articles"]
