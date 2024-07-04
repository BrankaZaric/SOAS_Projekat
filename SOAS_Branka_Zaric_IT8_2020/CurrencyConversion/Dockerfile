FROM openjdk:17
EXPOSE 8100
ADD target/currency-conversion-1.0.0.jar /app/currency-conversion.jar
ENTRYPOINT [ "java", "-jar", "/app/currency-conversion.jar" ]