# nsidr-api-tests
This Java project provides a set of automated tests against the nsidr.org API (Cordra API enhanced with [doec library](https://github.com/DiSSCo/doec)). The tests run against the HTTP Rest interface and the DOIP interface. They consist of two parts:

- Non-intrusive tests: Only check the system status, GET functions
- Intrusive tests: Tests CRUD functions with a test object

The intrusive tests should be run only on a test server, non-intrusive tests can also be run on production server

# To-Do
The way how CRUD functions are tests include a hard-corded Digital Specimen Object, based on this schema: https://nsidr.org/#objects/20.5000.1025/DigitalSpecimen_schema With the ongoing development on the [OpenDS schema](https://github.com/DiSSCo/openDS) the changes must be implemented in the tests in this project

# Build and Run
To execute the tests configuration must be provided: copy `cp config.properties.template config.properties` and fill in your credentials.
The package is built with maven. The idea is to have a stand-alone executable jar file to run the tests later at any point again. Therefore different possibilities exist:

1. B
mvn package -Dconfig.path=config.properties -DintrusiveTests=true
