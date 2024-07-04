FROM openjdk:17
EXPOSE 8000
ADD target/currency-exchange-1.0.0.jar /app/currency-exchange.jar
ENTRYPOINT [ "java", "-jar", "/app/currency-exchange.jar" ]