# nsidr-api-tests
This Java project provides a set of automated tests against the nsidr.org API (Cordra API enhanced with [doec library](https://github.com/DiSSCo/doec)). The tests run against the HTTP Rest interface and the DOIP interface. They consist of two parts:

- Non-intrusive tests: Only check the system status, GET functions
- Intrusive tests: Tests CRUD functions with a test object

While the non-intrusive tests can also be run to check the system status on the production server, the intrusive tests should be run on a test server only. The `config.properties` file includes one configuration parameter `cert_validation` which should always be set to true, unless the nsidr.org test server setup is deployed on your local machine with self-signed https certificates.

# To-Do
The way how CRUD functions are tested include a hard-corded Digital Specimen Object, based on this schema: https://nsidr.org/#objects/20.5000.1025/DigitalSpecimen_schema. With the ongoing development on the [OpenDS schema](https://github.com/DiSSCo/openDS) the changes on the schema must be implemented in the tests in this project.

# Build and Run
To execute the tests a configuration file must be provided: copy `cp config.properties.template config.properties` and fill in your credentials.
The package is built with maven. The idea is to have a stand-alone executable jar file to run the tests later at any point again. Therefore different possibilities exist to build and/or run. Two command line parameters must always be passed to Java to run the tests:
1. `config.path`: The path to the file with the configuration properties
2. `intrusiveTests`:`true/false` to control whether intrusive tests should be run.

#### 1. Build and run tests
```bash
mvn package -Dconfig.path=config.properties -DintrusiveTests=true
```

#### 2. Build - run tests separately
To run the tests separately without maven, the [JUnit Platform Console Standalone](https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.7.2/junit-platform-console-standalone-1.7.2.jar) jar file needs to be available.
```bash
mvn package -DskipTests
# This creates the file target/testing-0.1-test-jar-with-dependencies.jar
# Run later at any time with:
java -Dconfig.path=config.properties -DintrusiveTests=true -jar junit-platform-console-standalone.jar -cp target/nsidr-test-suite-0.1-test-jar-with-dependencies.jar --select-package eu.dissco.testing
```

## Other
This Java package's source code follows the [Google Java Styleguide](https://github.com/google/styleguide).
