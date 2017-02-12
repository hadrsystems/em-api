FROM tomcat:8-jre7

ENV EMAPI_DATA_DIR=/var/lib/emapi
ENV TOMCAT_DBCP_URL http://central.maven.org/maven2/org/apache/tomcat/tomcat-dbcp/7.0.30/tomcat-dbcp-7.0.30.jar

ADD $TOMCAT_DBCP_URL $CATALINA_HOME/lib/tomcat-dbcp-7.0.30.jar
COPY api-rest-service/target/em-api.war $CATALINA_HOME/webapps/.
COPY config/* /opt/data/nics/config/
COPY tomcat-config/* $CATALINA_HOME/conf/

VOLUME $EMAPI_DATA_DIR
