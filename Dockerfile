FROM amazoncorretto:17-alpine
ARG ARG_PROFILE
ENV PROFILE=${ARG_PROFILE}
ENV SPRING_PROFILES_ACTIVE=${ARG_PROFILE}
COPY build/libs/*.jar app.jar
ENTRYPOINT exec java -jar -Dspring.profiles.active=${PROFILE} /app.jar