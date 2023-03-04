FROM gradle:8.0.1-jdk17-alpine AS build

RUN mkdir /gradle
WORKDIR /gradle
COPY --chown=gradle:gradle . .
RUN gradle build --no-daemon

FROM amazoncorretto:17-alpine

RUN apk add --no-cache msttcorefonts-installer fontconfig
RUN update-ms-fonts


RUN mkdir /app
COPY --from=build /gradle/build/libs/*.jar /app/app.jar
ENV ENVIRONMENT=production
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
