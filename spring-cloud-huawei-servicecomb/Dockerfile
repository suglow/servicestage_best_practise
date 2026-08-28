# Generic multi-stage build for modules in the root Maven reactor.
# Usage: docker build --build-arg MODULE=product-service .
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

ARG MODULE
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    test -n "$MODULE" \
    && mvn -B -DskipTests -pl "$MODULE" -am package \
    && mkdir -p /output \
    && find "$MODULE/target" -maxdepth 1 -type f -name '*.jar' ! -name '*.jar.original' \
       -exec cp '{}' /output/app.jar \; \
    && test -s /output/app.jar

FROM eclipse-temurin:17-jre

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /output/app.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
