# NICS Emergency Mangement API

## Synopsis

The NICS Emergency Management API (EM-API) provides rest endpoints that support the NICS web client as well as the NICS
mobile application. Once deployed, em-api can be accessed from your server via /em-api/v1/.

## Dependencies
- nics-tools
- nics-common

## Building

```mvn package```

## Configuration

The EM-API requires the following configuration files:

* AMConfig.properties
* em-api.properties
* newUserEnabledTemplate.txt.erb
* openam.properties
* openam-tools.properties
* sso-tools.properties

These configuration files will be loaded from /opt/data/nics/config directory unless changed using the em-api-config.xml
file, the default version of which can be found at ```api-rest-service/src/main/resources/em-api-config.xml``` in the
source repository.

Additionally, the application expects to find data sources bound to the following names in the application server:

* jboss/sadisplayDatasource
* jboss/shapefileDatasource

These are javax.sql.DataSource objects.

## Docker Support

The EM-API provides a Dockerfile capable of running the application as a Docker container based on Tomcat.

Follow these steps to build the docker container:

1. Build the WAR file (see above)
1. Place the required application configuration files in the ```config``` directory
1. Place the required Tomcat configuration in the ```tomcat-config``` directory
1. Build the Docker image [using the Docker build command](https://docs.docker.com/engine/reference/commandline/build/)

## Documentation

Further documentation is available in the [NICS Common Wiki](https://github.com/tabordasolutions/nics-common/wiki).
