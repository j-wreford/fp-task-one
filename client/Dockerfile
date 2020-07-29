FROM openjdk:8-alpine

COPY target/uberjar/client.jar /client/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/client/app.jar"]
