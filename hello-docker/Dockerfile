# Start with a base image containing Java runtime
FROM openjdk:8-jdk-alpine

# Add Group and User
RUN addgroup -S prakashtestGrp && adduser -S spring -G prakashtestGrp

# Run using non root user
USER spring:prakashtestGrp

# Add Maintainer Info
LABEL maintainer="callicoder@gmail.com"


# Make port 8080 available to the world outside this container
EXPOSE 8080

#Set app home folder
ENV APP_HOME /app

#Possibility to set JVM options (https://www.oracle.com/technetwork/java/javase/tech/vmoptions-jsp-140102.html)
ENV JAVA_OPTS=""

#Create base app folder
RUN mkdir $APP_HOME

#Create folder to save configuration files
RUN mkdir $APP_HOME/config

#Create folder with application logs
RUN mkdir $APP_HOME/log

VOLUME $APP_HOME/log
VOLUME $APP_HOME/config

WORKDIR $APP_HOME

# The application's jar file
ARG WAR_FILE=target/hello-docker-test.war

# Add the application's jar to the container
ADD ${WAR_FILE} hello-docker-test.war

# Run the jar file 
ENTRYPOINT ["java","-jar","hello-docker-test.war"]
