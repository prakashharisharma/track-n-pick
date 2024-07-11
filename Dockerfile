# Start with a base image containing Java runtime
FROM eclipse-temurin:17-jdk-jammy

# Add Maintainer Info
LABEL maintainer="prakashharisharma@gmail.com"

#Set app home folder
ENV TNP_HOME /opt/tnp
ENV LOGS_HOME /opt/tnp/logs
ENV APP_HOME /tnphome
#Set Profile -- Override from cmd
ENV PORT 8088
#Create base app folder
RUN mkdir $APP_HOME
RUN mkdir $TNP_HOME
RUN mkdir $LOGS_HOME

# Add Group and User
RUN addgroup tnp && useradd tnp-admin -G tnp

RUN chown tnp-admin:tnp $APP_HOME
RUN chown tnp-admin:tnp $TNP_HOME
RUN chown tnp-admin:tnp $LOGS_HOME

# Run using non root user
USER tnp-admin:tnp

# Make port 8080 available to the world outside this container
EXPOSE ${PORT}

#Possibility to set JVM options (https://www.oracle.com/technetwork/java/javase/tech/vmoptions-jsp-140102.html)
ENV JAVA_OPTS=""

WORKDIR $APP_HOME

# The application's jar file
ARG WAR_FILE=tnp-web-ui/target/tnp.war

# Add the application's jar to the container
ADD ${WAR_FILE} tnp.war

# Run the jar file 
ENTRYPOINT ["java","-jar","-Dserver.port=${PORT}","tnp.war"]
