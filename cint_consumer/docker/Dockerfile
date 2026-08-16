# Stage 1: build jar
FROM gradle:jdk25 AS build
WORKDIR /root/project
COPY --from=m2repo /org/example/cint_common /root/.m2/repository/org/example/cint_common
COPY . .
RUN gradle bootJar --no-daemon

# Stage 2: runtime image
FROM eclipse-temurin:25-jre
WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

COPY --from=build /root/project/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
