FROM openjdk:17
EXPOSE 8500
ADD target/crypto-conversion-1.0.0.jar /app/crypto-conversion.jar
ENTRYPOINT [ "java", "-jar", "/app/crypto-conversion.jar" ]