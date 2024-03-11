FROM openjdk:17
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} xseed-api-gateway.jar
ENTRYPOINT ["java","-jar","/xseed-api-gateway.jar"]
EXPOSE 8085