FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace/backend

COPY backend/ ./

RUN chmod +x gradlew && ./gradlew --no-daemon api:bootJar

FROM eclipse-temurin:21-jre

# Selenium crawler support for production requests that need a browser runtime.
RUN apt-get update \
  && apt-get install -y --no-install-recommends \
    chromium \
    fonts-noto-cjk \
    libatk-bridge2.0-0 \
    libatk1.0-0 \
    libatspi2.0-0 \
    libcups2 \
    libdrm2 \
    libgbm1 \
    libgtk-3-0 \
    libnspr4 \
    libnss3 \
    libu2f-udev \
    libxcomposite1 \
    libxdamage1 \
    libxfixes3 \
    libxkbcommon0 \
    libxrandr2 \
    xdg-utils \
  && rm -rf /var/lib/apt/lists/*

ENV CHROME_BIN=/usr/bin/chromium
ENV JAVA_TOOL_OPTIONS="-Xms256M -Xmx256M -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/heapdumps -XX:OnOutOfMemoryError=kill -9 %p"

WORKDIR /app

COPY --from=build /workspace/backend/api/build/libs/*.jar /app/app.jar

RUN mkdir -p /app/heapdumps

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
CMD ["--spring.profiles.active=prod"]

