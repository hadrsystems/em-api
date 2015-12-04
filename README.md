## Synopsis

Emergency Management API (EM-API) provides rest endpoints that support the NICS web client as well as the NICS mobile application. Once deployed, em-api can be accessed from your server via /em-api/v1/   

## Dependencies
- nics-tools
- nics-common

## Building

    mvn install

## Configuration

The configuration file can be found here em-api/api-rest-service/src/main/config. The default location for the configuration file is /opt/data/nics/config directory. To change the location please update the em-api/api-rest-service/src/main/resources/em-api-config.xml file.

The database information must be set in the em-api/api-rest-service/src/main/webapp/META-INF/context.xml file before compiling and deploying the application

## Documentation

Further documentation is available at nics-common/docs
