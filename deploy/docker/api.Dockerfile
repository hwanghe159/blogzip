FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace/backend

COPY backend/ ./

RUN chmod +x gradlew && ./gradlew --no-daemon api:bootJar

FROM eclipse-temurin:21-jre

ENV JAVA_TOOL_OPTIONS="-Xms256M -Xmx256M -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/heapdumps -XX:OnOutOfMemoryError=kill -9 %p"

WORKDIR /app

COPY --from=build /workspace/backend/api/build/libs/*.jar /app/app.jar

RUN mkdir -p /app/heapdumps

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
CMD ["--spring.profiles.active=prod"]
