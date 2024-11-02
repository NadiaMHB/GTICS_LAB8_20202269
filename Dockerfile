FROM openjdk:24-ea-17-jdk
VOLUME /tmp
EXPOSE 8080
ADD ./target/lab8-20202269-0.0.1-SNAPSHOT.jar lab8.jar
ENTRYPOINT ["java","-jar","lab8.jar"]