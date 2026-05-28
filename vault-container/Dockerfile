### ===== Stage 1: Maven Build =====
FROM hub.bmicc.ir/library/maven:3.9.6-eclipse-temurin-21 AS maven_builder

ARG NEXUS_USERNAME
ARG NEXUS_PASSWORD

RUN mkdir -p /root/.m2 && \
    echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"' > /root/.m2/settings.xml && \
    echo '          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"' >> /root/.m2/settings.xml && \
    echo '          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">' >> /root/.m2/settings.xml && \
    echo '  <servers><server><id>private-nexus</id>' >> /root/.m2/settings.xml && \
    echo "      <username>${NEXUS_USERNAME}</username>" >> /root/.m2/settings.xml && \
    echo "      <password>${NEXUS_PASSWORD}</password>" >> /root/.m2/settings.xml && \
    echo '  </server></servers>' >> /root/.m2/settings.xml && \
    echo '  <mirrors><mirror><id>internal_nexus</id>' >> /root/.m2/settings.xml && \
    echo '      <mirrorOf>*</mirrorOf>' >> /root/.m2/settings.xml && \
    echo '      <url>http://repo.bmicc.ir:8081/repository/maven-public/</url>' >> /root/.m2/settings.xml && \
    echo '  </mirror></mirrors></settings>' >> /root/.m2/settings.xml

WORKDIR /app
COPY ../../ .
RUN mvn -B clean package -Pstaging -DskipTests -Dmaven.test.skip=true -U

# پیدا کردن اولین JAR و کپی به اسم ثابت
RUN JAR_FILE=$(find /app -type f -name "*.jar" | head -n 1) \
 && echo "Found JAR: $JAR_FILE" \
 && cp "$JAR_FILE" /app/app.jar

### ===== Stage 2: Runtime =====
FROM hub.bmicc.ir/library/eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app
# حالا مسیر ثابت رو کپی می‌کنیم
COPY --from=maven_builder /app/vault-service/vault-container/target/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=staging
EXPOSE 8012
CMD ["java", "-jar", "app.jar"]