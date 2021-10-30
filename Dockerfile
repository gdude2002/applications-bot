FROM openjdk:16-jdk-slim

COPY build/libs/applications-bot-*-all.jar /usr/local/lib/applications-bot.jar

RUN mkdir /bot
RUN mkdir /data
WORKDIR /bot

ENV DATA_DIR /data

ENTRYPOINT ["java", "-Xms2G", "-Xmx2G", "-jar", "/usr/local/lib/applications-bot.jar"]
