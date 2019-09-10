FROM library/openjdk:12-alpine

WORKDIR app

COPY . .

RUN ["./gradlew", "build"]