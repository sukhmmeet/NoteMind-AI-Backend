FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle

RUN chmod +x gradlew

COPY src ./src

RUN ./gradlew bootJar --no-daemon \
    && find build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' -exec cp {} build/libs/app.jar \;

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN useradd --system --create-home --home-dir /app appuser

COPY --from=build /workspace/build/libs/app.jar ./app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
