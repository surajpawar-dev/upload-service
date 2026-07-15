
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml ./
COPY .mvn .mvn
RUN mvn -B -ntp dependency:go-offline

COPY src src
RUN mvn -B -ntp package

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN groupadd --system app && useradd --system --gid app --home-dir /app app

COPY --from=build /workspace/target/upload-platform-0.0.1-SNAPSHOT.jar /app/upload-platform.jar

RUN mkdir -p /app/data/uploads && chown -R app:app /app

USER app

EXPOSE 8080

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport" \
    AWS_S3_ENABLED=false \
    OPENSEARCH_ENABLED=false \
    SPRING_DATA_ELASTICSEARCH_REPOSITORIES_ENABLED=false \
    STORAGE_LOCAL_DIRECTORY=/app/data/uploads \
    UPLOAD_SMALL_FILE_MAX_BYTES=104857600 \
    UPLOAD_MULTIPART_MIN_BYTES=104857601 \
    UPLOAD_MAX_FILE_SIZE_BYTES=104857600

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/upload-platform.jar"]
