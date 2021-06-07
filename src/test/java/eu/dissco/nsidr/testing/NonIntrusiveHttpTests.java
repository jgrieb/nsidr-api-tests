package eu.dissco.nsidr.testing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;

// import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.equalTo;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NonIntrusiveHttpTests extends HttpTests {

  @Test
  @Order(1)
  public void cordraHttpStartupStatusIsOk() {
    this.getLogger().info("##### Check that Cordra startup status is OK #####");
    this.startRequest().get("startupStatus").then().statusCode(200).assertThat()
        .body("state", equalTo("UP")).assertThat().body("details.storage", equalTo("UP"))
        .assertThat().body("details.indexer", equalTo("UP"));
    this.getLogger().info("---- Test success -----\n");

    String provHostAddress = this.getConfig().getString("provenanceRepository.url");
    this.startRequest().get(String.format("%1$s/startupStatus", provHostAddress)).then()
        .statusCode(200).assertThat().body("state", equalTo("UP")).assertThat()
        .body("details.storage", equalTo("UP")).assertThat().body("details.indexer", equalTo("UP"));
    this.getLogger().info("---- Test success -----\n");
  }

  @Test
  @Order(2)
  public void postSchemaWithoutAuthorizationShouldNotBeAllowed() {
    this.getLogger().info("##### Check that authorization works #####");
    this.startRequest().given().contentType("application/json").body("dummyParameter").when()
        .post("/schemas").then().statusCode(401);
    this.getLogger().info("---- Test success -----\n");
  }

  @Test
  @Order(3)
  public void DigitalSpecimen_schemaIsFound() {
    this.getLogger().info("##### Check that DigitalSpecimen_schema is found #####");
    String prefix = this.getConfig().getString("digitalObjectRepository.handlePrefix");
    this.startRequest().get(String.format("/objects/%1$s/DigitalSpecimen_schema", prefix)).then()
        .statusCode(200);
    this.getLogger().info("---- Test success -----\n");
  }
}
