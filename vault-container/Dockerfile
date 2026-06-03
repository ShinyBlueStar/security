# syntax=docker/dockerfile:1

### ===== Stage 1: Maven Build =====
FROM hub.bmicc.ir/library/maven:3.9.6-eclipse-temurin-21 AS maven_builder

WORKDIR /app
COPY . .

# Nexus credentials come from a BuildKit secret mount, so they exist only for the
# duration of this RUN step and are never written into an image layer or build cache.
# The mounted file is a full Maven settings.xml (servers + mirror), e.g.:
#   docker build --secret id=maven_settings,src=/path/to/settings.xml -f vault-container/Dockerfile .
RUN --mount=type=secret,id=maven_settings,target=/root/.m2/settings.xml,required=true \
    mvn -B clean package -Pstaging -DskipTests -Dmaven.test.skip=true -U

RUN JAR_FILE=$(find /app -type f -name "*.jar" | head -n 1) \
 && echo "Found JAR: $JAR_FILE" \
 && cp "$JAR_FILE" /app/app.jar

### ===== Stage 2: Runtime =====
FROM hub.bmicc.ir/library/eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app
COPY --from=maven_builder /app/vault-service/vault-container/target/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=staging
EXPOSE 8012
CMD ["java", "-jar", "app.jar"]
