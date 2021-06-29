
FROM openjdk:16-alpine3.13
# ----
# Install Maven
RUN apk add --no-cache curl tar bash
ARG MAVEN_VERSION=3.6.3
ARG USER_HOME_DIR="/root"
RUN mkdir -p /usr/share/maven && \
curl -fsSL http://apache.osuosl.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar -xzC /usr/share/maven --strip-components=1 && \
ln -s /usr/share/maven/bin/mvn /usr/bin/mvn
ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"
# speed up Maven JVM a bit
ENV MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"

# ----
# Install project dependencies and keep sources
# make source folder
RUN mkdir -p /usr/src/app/Backend
WORKDIR /usr/src/app/Backend
# install maven dependency packages (keep in image)
COPY ./pom.xml /usr/src/app/Backend
RUN mvn clean
RUN mvn -T 1C install -DskipTests && rm -rf target
# copy other source files (keep in image)
# COPY src/main/java/NettyWebServer /usr/src/app/Backend/src/main/java/NettyWebServer
# COPY src/main/java/Chat /usr/src/app/Backend/src/main/java/Chat
# COPY src/main/java/Config /usr/src/app/Backend/src/main/java/Config
# COPY src/main/java/MediaServer /usr/src/app/Backend/src/main/java/MediaServer
# COPY src/main/resources /usr/src/app/Backend/src/main/resources
COPY src /usr/src/app/Backend/src
RUN mvn compile
RUN mvn package -DskipTests
ENTRYPOINT ["mvn","exec:java","-D","exec.mainClass=NettyWebServer.NettyServer"]
# RUN java -cp target/Backend-1.0-SNAPSHOT.jar NettyWebServer.NettyServer
# CMD ["mvn","java"]
