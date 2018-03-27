FROM openjdk:8-jdk
RUN mkdir -p /moola-server
WORKDIR /tmp
RUN wget http://www-eu.apache.org/dist/tomcat/tomcat-8/v8.5.28/bin/apache-tomcat-8.5.28.tar.gz
RUN tar -xvzf apache-tomcat-8.5.28.tar.gz
RUN mv apache-tomcat-8.5.28 /moola-server/tomcat
RUN sed -i -e 's/port="8080"/port="8080" address="0.0.0.0"/' /moola-server/tomcat/conf/server.xml
WORKDIR /moola-server

EXPOSE 8080
ENTRYPOINT ["tomcat/bin/catalina.sh"]
CMD ["run"]
