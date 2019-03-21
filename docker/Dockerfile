FROM openjdk:8-alpine

ADD dist/*.jar app.jar

RUN touch /app.jar

ENV SPRING_ACTIVE_PROFILE cloud
ENV JAVA_ARGS '-Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=${SPRING_ACTIVE_PROFILE}'
ENV JAVA_OPTS '-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:MaxRAMFraction=2'
ENV JAVA_DEBUG ''

ENTRYPOINT ["/bin/sh", "-c", "java ${JAVA_ARGS} ${JAVA_OPTS} ${JAVA_DEBUG} -jar /app.jar"]