FROM eclipse-temurin:17-alpine

RUN mkdir /source
ADD target/ISSNormalizeServer-*.jar source/ISSServer.jar
ADD docker-entrypoint.sh /entrypoint.sh

RUN mkdir /server

EXPOSE 7000

ENTRYPOINT ["/bin/ash", "/entrypoint.sh"]