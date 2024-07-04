FROM openjdk:17
EXPOSE 8770
ADD target/users-service-1.0.0.jar /app/users-service.jar
ENTRYPOINT [ "java", "-jar", "/app/users-service.jar" ]