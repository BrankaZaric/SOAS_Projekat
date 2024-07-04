FROM openjdk:17
EXPOSE 8600
ADD target/trade-service-1.0.0.jar /app/trade-service.jar
ENTRYPOINT [ "java", "-jar", "/app/trade-service.jar" ]