FROM gradle:8.0.1-jdk17-alpine AS build

RUN mkdir /gradle
WORKDIR /gradle
COPY --chown=gradle:gradle . .
RUN gradle build --no-daemon

FROM openjdk:17

RUN mkdir /app
COPY --from=build /gradle/build/libs/*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
