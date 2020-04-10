# Start with a base image containing Java runtime
FROM openjdk:11

# Add Maintainer Info
LABEL maintainer="prakashharisharma@gmail.com"

#Set app home folder
ENV TNP_HOME /opt/tnp
ENV APP_HOME /tnphome

#Create base app folder
RUN mkdir $APP_HOME

# Add Group and User
RUN addgroup tnp && useradd tnp-admin -G tnp

RUN chown tnp-admin:tnp $APP_HOME

# Run using non root user
USER tnp-admin:tnp

# Make port 8080 available to the world outside this container
EXPOSE 8080

#Possibility to set JVM options (https://www.oracle.com/technetwork/java/javase/tech/vmoptions-jsp-140102.html)
ENV JAVA_OPTS=""

WORKDIR $APP_HOME

# The application's jar file
ARG WAR_FILE=tnp-web-ui/target/tnp.war

# Add the application's jar to the container
ADD ${WAR_FILE} tnp.war

# Run the jar file 
ENTRYPOINT ["java","-jar","tnp.war"]
