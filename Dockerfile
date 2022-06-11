FROM openjdk:8-jdk-alpine
WORKDIR /controller
ADD target/controller.jar controller.jar
COPY Data Data
COPY src/main/webapp src/main/webapp
EXPOSE 8080
ENTRYPOINT ["java","-jar","controller.jar"]
