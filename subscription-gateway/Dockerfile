FROM openjdk:8-jdk-alpine

COPY target/subscription-gateway*.jar /opt
EXPOSE 9998

ENTRYPOINT ["java", "-cp", "/opt/*", "org.springframework.boot.loader.JarLauncher"]
