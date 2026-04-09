FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace/backend

COPY backend/ ./

RUN chmod +x gradlew && ./gradlew --no-daemon batch:bootJar

FROM eclipse-temurin:21-jre

RUN apt-get update \
  && apt-get install -y --no-install-recommends cron util-linux tzdata \
  && rm -rf /var/lib/apt/lists/*

ENV JAVA_TOOL_OPTIONS="-Xms256M -Xmx256M -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/heapdumps -XX:+ExitOnOutOfMemoryError"
ENV TZ=Asia/Seoul

WORKDIR /app

COPY --from=build /workspace/backend/batch/build/libs/*.jar /app/app.jar
COPY deploy/scheduler/crontab /app/crontab
COPY deploy/scheduler/entrypoint.sh /app/entrypoint.sh

RUN chmod +x /app/entrypoint.sh \
  && chmod 0644 /app/crontab \
  && mkdir -p /app/heapdumps

CMD ["/app/entrypoint.sh"]
