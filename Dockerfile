# 1) Frontend: gera o bundle a partir do código, não do que estiver versionado em static/.
FROM node:24-slim AS web
WORKDIR /web
RUN corepack enable
COPY frontend/package.json frontend/pnpm-lock.yaml frontend/pnpm-workspace.yaml ./
RUN pnpm install --frozen-lockfile
COPY frontend/ ./
RUN pnpm run build

# 2) Backend: empacota o Spring Boot com o bundle novo. Os testes rodam no CI, não aqui.
FROM maven:3.9-eclipse-temurin-21 AS app
WORKDIR /src
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY src/ src/
RUN rm -rf src/main/resources/static/assets
COPY --from=web /web/dist/ src/main/resources/static/
RUN mvn -B -q package -Dmaven.test.skip=true

# 3) Execução: só o JRE e o jar, com usuário sem privilégios.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S sputnik && adduser -S sputnik -G sputnik && chown sputnik:sputnik /app
COPY --from=app /src/target/sputnik-*.jar app.jar
USER sputnik
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=postgres
HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=5 \
  CMD wget -q -O /dev/null http://localhost:8080/login || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
