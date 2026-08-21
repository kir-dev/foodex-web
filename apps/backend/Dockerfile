FROM gradle:jdk25 as build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle clean assemble bootJar

FROM eclipse-temurin:25-jre-alpine
MAINTAINER kir-dev@sch.bme.hu
COPY --from=build /home/gradle/src/build/libs/foodex.jar /opt/foodex/
WORKDIR /opt/foodex

RUN java -Djarmode=tools -jar foodex.jar extract --destination application
RUN rm foodex.jar

WORKDIR /opt/foodex/application
RUN java "-Dspring.profiles.include=prewarm" "-XX:ArchiveClassesAtExit=application.jsa" "-XX:+UnlockExperimentalVMOptions" "-XX:+UseContainerSupport" "-XX:+UseSerialGC" "-XX:MaxRAMPercentage=90" "-Dspring.context.exit=onRefresh" -jar foodex.jar

ENTRYPOINT ["java", "-Dspring.profiles.include=env", "-XX:SharedArchiveFile=application.jsa", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=70", "-jar", "/opt/foodex/application/foodex.jar"]
EXPOSE 80
