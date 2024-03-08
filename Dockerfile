FROM openjdk:8
EXPOSE 8080
ADD target/attendance-command-service.jar attendance-command-service.jar 
ENTRYPOINT ["java","-jar","/order-service.jar"]