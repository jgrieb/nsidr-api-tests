package eu.dissco.nsidr.testing;

import eu.dissco.doec.digitalObjectRepository.DigitalObjectRepositoryClient;
import eu.dissco.doec.digitalObjectRepository.DigitalObjectRepositoryInfo;
import eu.dissco.doec.digitalObjectRepository.DigitalObjectRepositoryException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DoipTests {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private DigitalObjectRepositoryClient doipClient;
  private Configuration config;

  protected Configuration getConfig() {
    return config;
  }

  protected DigitalObjectRepositoryClient getDoipClient() {
    return this.doipClient;
  }

  protected Logger getLogger() {
    return this.logger;
  }

  protected void setConfig(Configuration config) {
    this.config = config;
  }

  protected void setDoipClient(DigitalObjectRepositoryClient doipClient) {
    this.doipClient = doipClient;
  }

  @BeforeAll
  public void setup() throws DigitalObjectRepositoryException, ConfigurationException {
    this.setConfig(TestUtils.loadTestConfig());

    Configuration c = this.getConfig();
    String url = c.getString("digitalObjectRepository.url");
    this.getLogger().info("Started DoipTests on server " + url);

    int doipPort = c.getInt("digitalObjectRepository.doipPort");
    String handlePrefix = c.getString("digitalObjectRepository.handlePrefix");
    String username = c.getString("digitalObjectRepository.username");
    String password = c.getString("digitalObjectRepository.password");
    int pageSize = c.getInt("digitalObjectRepository.searchPageSize");
    if(password == null || password.isEmpty()){
      // if the password is not provided we set credentials to null to prevent auth checking
      username = null;
      password = null;
    }
    DigitalObjectRepositoryInfo info =
        new DigitalObjectRepositoryInfo(url, doipPort, handlePrefix, username, password, pageSize);
    this.setDoipClient(new DigitalObjectRepositoryClient(info));
  }

  @AfterAll
  public void tearDown() {
    this.doipClient.close();
  }
}
