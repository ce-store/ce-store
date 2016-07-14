FROM tomcat:7

RUN rm -rf /usr/local/tomcat/webapps/*
ADD target/ce-store-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ce-store.war

CMD ["catalina.sh", "run"]
