FROM openjdk:17
EXPOSE 8765
ADD target/api-gateway-1.0.0.jar /app/api-gateway.jar
ENTRYPOINT [ "java", "-jar", "/app/api-gateway.jar" ]