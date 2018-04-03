# Base Alpine Linux based image with OpenJDK JRE only
FROM oracle/serverjre:8
# copy application WAR (with libraries inside)
COPY target/service.jar /app.jar
# specify default command
CMD ["/usr/bin/java", "-jar", "/app.jar"]
