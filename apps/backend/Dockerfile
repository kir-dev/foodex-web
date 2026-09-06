FROM gradle:jdk25 AS build
WORKDIR /home/gradle/src
# The gradle image volume-mounts /home/gradle/.gradle, so cache written there
# is discarded between RUN layers. Keep it on a normal path instead.
ENV GRADLE_USER_HOME=/home/gradle/cache

COPY --chown=gradle:gradle gradle/ gradle/
COPY --chown=gradle:gradle gradlew settings.gradle.kts build.gradle.kts ./
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies

COPY --chown=gradle:gradle src/ src/
RUN ./gradlew --no-daemon bootJar -x test

FROM eclipse-temurin:25-jre-alpine
MAINTAINER kir-dev@sch.bme.hu
RUN apk add --no-cache tzdata
ENV TZ=Europe/Budapest
# Classic builder caches COPY --from by instruction text. Referencing
# JAR_CACHE_ID here forces a fresh JAR copy when deploy passes a new hash.
ARG JAR_CACHE_ID=0
RUN echo "jar.cache.id=${JAR_CACHE_ID}"
COPY --from=build /home/gradle/src/build/libs/foodex.jar /opt/foodex/
WORKDIR /opt/foodex

RUN java -Djarmode=tools -jar foodex.jar extract --destination application
RUN rm foodex.jar

WORKDIR /opt/foodex/application
RUN java "-Dspring.profiles.include=prewarm" "-XX:ArchiveClassesAtExit=application.jsa" "-XX:+UnlockExperimentalVMOptions" "-XX:+UseContainerSupport" "-XX:+UseSerialGC" "-XX:MaxRAMPercentage=90" "-Dspring.context.exit=onRefresh" -jar foodex.jar

ENTRYPOINT ["java", "-Dspring.profiles.include=env", "-XX:SharedArchiveFile=application.jsa", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=70", "-jar", "/opt/foodex/application/foodex.jar"]
EXPOSE 80
