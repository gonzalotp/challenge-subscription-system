FROM openjdk:8-jdk-alpine

COPY target/subscription-backend*.jar /opt
EXPOSE 9999

ENTRYPOINT ["java", "-cp", "/opt/*", "org.springframework.boot.loader.JarLauncher"]
