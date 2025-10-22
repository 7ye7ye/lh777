FROM registry-1.docker.io/java:21-anolis

MAINTAINER MoseKali

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

#RUN mkdir -p /jeecg-boot/config/jeecg/

WORKDIR /silos-boot

EXPOSE 8080

#ADD ./src/main/resources/jeecg ./config/jeecg
ADD ./target/silos-system-start-3.8.1.jar ./

CMD sleep 60;java -Djava.security.egd=file:/dev/./urandom -jar jeecg-system-start-3.8.1.jar