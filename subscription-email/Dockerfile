FROM openjdk:8-jdk-alpine

COPY target/subscription-email*.jar /opt

ENTRYPOINT ["java", "-cp", "/opt/*", "org.springframework.boot.loader.JarLauncher"]
