# syntax=docker/dockerfile:1.7
# Build one Maven module: docker build --build-arg MODULE=product-service .
FROM maven:3.9.9-eclipse-temurin-8 AS build

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

FROM eclipse-temurin:8-jre AS runtime-base
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /output/app.jar app.jar

FROM runtime-base AS plain-runtime
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

FROM alpine:3.20 AS sermant-download
ARG SERMANT_VERSION=2.0.0
ARG SERMANT_SHA256=a2c2e532f2cb3c85e8da3cb0718fa0ea5e81d35eeeb77eafb6e30cdeabb67855
ARG SERMANT_URL=https://github.com/sermant-io/Sermant/releases/download/v${SERMANT_VERSION}/sermant-${SERMANT_VERSION}.tar.gz
RUN apk add --no-cache curl tar \
    && curl -fsSL --retry 3 -o /tmp/sermant.tar.gz "$SERMANT_URL" \
    && echo "$SERMANT_SHA256  /tmp/sermant.tar.gz" | sha256sum -c - \
    && mkdir -p /opt/sermant \
    && tar -xzf /tmp/sermant.tar.gz -C /opt/sermant --strip-components=1 \
    && rm /tmp/sermant.tar.gz

FROM runtime-base AS sermant-runtime
COPY --from=sermant-download /opt/sermant /opt/sermant
RUN sed -i 's|address: http://localhost:30100|address: http://local-cse:30100|g' \
        /opt/sermant/agent/pluginPackage/service-registry/config/config.yaml \
    && sed -i 's|enableSpringRegister: false|enableSpringRegister: true|g' \
        /opt/sermant/agent/pluginPackage/service-registry/config/config.yaml \
    && sed -i 's|agent.service.dynamic.config.enable=true|agent.service.dynamic.config.enable=false|g' \
        /opt/sermant/agent/config/config.properties \
    && sed -i 's|dynamic.config.dynamicConfigType=ZOOKEEPER|dynamic.config.dynamicConfigType=NOP|g' \
        /opt/sermant/agent/config/config.properties \
    && grep -q 'address: http://local-cse:30100' /opt/sermant/agent/pluginPackage/service-registry/config/config.yaml \
    && grep -q 'enableSpringRegister: true' /opt/sermant/agent/pluginPackage/service-registry/config/config.yaml
ENTRYPOINT ["sh", "-c", "exec java -javaagent:/opt/sermant/agent/sermant-agent.jar -Dservice.meta.application=\"${CAS_APPLICATION_NAME:-demo-application}\" -Dservice.meta.version=\"${CAS_INSTANCE_VERSION:-1.0.0}\" -jar /app/app.jar"]
