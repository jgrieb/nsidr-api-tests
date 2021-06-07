package eu.dissco.nsidr.testing;

import eu.dissco.doec.digitalObjectRepository.DigitalObjectRepositoryException;
import net.dona.doip.client.DigitalObject;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NonIntrusiveDoipTests extends DoipTests {
  /*
   * If no connection can be established each tests fails after 60s This network timeout value is
   * currently hardcoded into the doip.sdk library and can therefore not be changed
   */

  @Test
  @Order(1)
  public void testDOIPHelloOperation() throws DigitalObjectRepositoryException {
    // throws error if DOIP server cannot be reached or return value is null
    this.getLogger().info("##### DOIP Hello #####");
    DigitalObject hello = this.getDoipClient().hello();
    assertThat(hello, notNullValue());
    Double protocolVersion = hello.attributes.get("protocolVersion").getAsDouble();
    assertThat(protocolVersion, greaterThanOrEqualTo(2.0));
    this.getLogger().info("---- Test success -----\n");
  }

  @Test
  @Order(2)
  public void testDOIPListOperation() throws DigitalObjectRepositoryException {
    this.getLogger().info("##### DOIP ListOperations #####");
    List<String> operations = this.getDoipClient().listOperations();
    assertThat(operations.size(), greaterThan(1));
    this.getLogger().info("---- Test success -----\n");
  }

  @Test
  @Order(3)
  public void retrieveDigitalSpecimenSchema() throws DigitalObjectRepositoryException {
    String prefix = this.getConfig().getString("digitalObjectRepository.handlePrefix");
    this.getLogger().info("##### DOIP Retrieve DigitalSpecimen_schema #####");
    String schemaId = String.format("%1$s/DigitalSpecimen_schema", prefix);
    DigitalObject oDSSchema = this.getDoipClient().retrieve(schemaId);
    assertThat(oDSSchema.attributes.getAsJsonObject("content").get("identifier").getAsString(),
        equalTo(schemaId));
    this.getLogger().info("---- Test success -----\n");
  }
}
