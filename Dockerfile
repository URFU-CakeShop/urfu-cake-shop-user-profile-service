FROM gradle:9.3.1-jdk17-corretto AS builder

WORKDIR /app/urfu-cake-shop-user-profile-service
COPY build.gradle .
COPY settings.gradle .
COPY src src

RUN gradle bootJar --no-daemon

FROM amazoncorretto:17-alpine
WORKDIR /app
COPY --from=builder /app/urfu-cake-shop-user-profile-service/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]