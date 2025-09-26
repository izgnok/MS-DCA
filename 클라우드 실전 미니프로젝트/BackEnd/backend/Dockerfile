# 1단계: 빌드
FROM gradle:8.10.2-jdk17-jammy AS builder
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon || true

COPY . .
RUN ./gradlew bootJar --no-daemon

# 2단계: 런타임 (멀티아키 지원 JRE)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]