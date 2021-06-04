package eu.dissco.nsidr.testing;

import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.config.SSLConfig;
import io.restassured.specification.RequestSpecification;
import static io.restassured.RestAssured.given;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HttpTests {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private Configuration config;

  protected Configuration getConfig() {
    return config;
  }

  protected Logger getLogger() {
    return this.logger;
  }

  protected void setConfig(Configuration config) {
    this.config = config;
  }

  @BeforeAll
  public void setup() throws ConfigurationException {
    this.setConfig(TestUtils.loadTestConfig());

    String url = this.getConfig().getString("digitalObjectRepository.url");
    this.getLogger().info("Started HttpTests on server " + url);

    RestAssured.baseURI = url;
    /*
     * RestAssured.basePath = "/"; if (port != null) { RestAssured.port = Integer.valueOf(port); }
     */
  }

  public RequestSpecification startRequest() {
    Boolean validateCerts = this.getConfig().getBoolean("cert_validation");
    if (!validateCerts) {
      return given().config(
          RestAssuredConfig.newConfig().sslConfig(new SSLConfig().relaxedHTTPSValidation()));
    } else {
      return given();
    }
  }

  public RequestSpecification startRequestAuth() {
    String user = this.getConfig().getString("digitalObjectRepository.username");
    String password = this.getConfig().getString("digitalObjectRepository.password");
    return this.startRequest().auth().preemptive().basic(user, password);
  }
}
