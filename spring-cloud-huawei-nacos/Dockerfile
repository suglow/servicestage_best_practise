FROM maven:3.9-eclipse-temurin-17 AS build-all

WORKDIR /build

COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests package

FROM build-all AS select-module

ARG MODULE
RUN test -n "$MODULE" \
    && mkdir -p /output \
    && test "$(find "$MODULE/target" -maxdepth 1 -type f -name '*.jar' ! -name '*.jar.original' | wc -l)" -eq 1 \
    && find "$MODULE/target" -maxdepth 1 -type f -name '*.jar' ! -name '*.jar.original' \
       -exec cp '{}' /output/app.jar \; \
    && test -s /output/app.jar

FROM eclipse-temurin:17-jre

RUN command -v curl \
    && groupadd --gid 10001 appuser \
    && useradd --uid 10001 --gid 10001 --no-create-home --shell /usr/sbin/nologin appuser

WORKDIR /app
COPY --from=select-module /output/app.jar app.jar

USER 10001

ENTRYPOINT ["java", "-jar", "app.jar"]
