FROM openjdk:11-jdk-slim as buildwar
MAINTAINER Chris Peck <crpeck@wm.edu>
RUN cd /tmp \
  && apt-get update \
  && apt-get install -y --no-install-recommends git gradle \
  && git clone -b 6.3 --single-branch https://github.com/apereo/cas-overlay-template.git cas-overlay \
  && mkdir -p /tmp/cas-overlay/src/main/webapp
WORKDIR /tmp/cas-overlay
COPY src/ /tmp/cas-overlay/src
COPY build.gradle /tmp/cas-overlay/build.gradle
RUN  ./gradlew clean build

FROM openjdk:11-jre-slim
MAINTAINER Chris Peck <crpeck@wm.edu>
RUN mkdir /etc/cas \
  && cd /etc/cas
WORKDIR /root
COPY --from=buildwar /tmp/cas-overlay/build/libs/cas.war .
#COPY etc/cas /etc/cas
EXPOSE 8080
CMD [ "java", "-jar", "/root/cas.war" ]
