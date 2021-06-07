package eu.dissco.nsidr.testing;

import io.restassured.response.ValidatableResponse;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasEntry;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntrusiveHttpTests extends HttpTests {
  // To-Do: definedInstanceIdSuffix script currently contains a hardcoded id,
  // because this is necessary for doec for lifecycle actions. Should be
  // replaced by using generic returned ID in the future.
  public final String definedInstanceIdSuffix = "test_ods_instance_http";
  private String createdInstanceId;

  @Test
  @EnabledIfSystemProperty(named = "intrusiveTests", matches = "true")
  public void ODS_crud_operations_test() {
    this.getLogger().info("##### starting ODS_crud_operations_test via HTTP #####\n");
    this.getLogger().info("----- CREATE -----");
    /*
     * CREATE { "id": "{{ cordra.cordra_nsidr.prefix }}/test_ods_instance_http", "midslevel": 0,
     * "institutionCode": ["CU"], "physicalSpecimenId": "test_physicalSpecimenId", "scientificName":
     * "test_scientificName" }
     */
    HashMap<String, Object> create_json = new HashMap<>();
    String prefix = this.getConfig().getString("digitalObjectRepository.handlePrefix");
    create_json.put("id", prefix + "/" + definedInstanceIdSuffix);
    create_json.put("midslevel", 0);
    ArrayList<String> values = new ArrayList<String>();
    values.add("CU");
    create_json.put("institutionCode", values);
    create_json.put("physicalSpecimenId", "test_physicalSpecimenId");
    create_json.put("scientificName", "test_scientificName");

    ValidatableResponse response =
        this.startRequestAuth().contentType("application/json").body(create_json)
            .post("/objects?type=DigitalSpecimen&suffix=" + definedInstanceIdSuffix).then();
    response.log().all();
    response.statusCode(200);
    String createdId = response.extract().path("id");
    this.createdInstanceId = createdId;
    this.getLogger().info("---- Test success -----\n");

    // Wait 5 seconds for the provenance life cycle hooks to be activated
    try {
      TimeUnit.SECONDS.sleep(5);
    } catch (InterruptedException e) {
    }

    /*
     * UPDATE { "physicalSpecimenId": "test_changed_physicalSpecimenId" }
     */
    this.getLogger().info("----- UPDATE -----");
    HashMap<String, Object> update_json = create_json;
    update_json.put("physicalSpecimenId", "test_changed_physicalSpecimenId");

    response = this.startRequestAuth().contentType("application/json").body(update_json)
        .put("/objects/" + createdId).then();
    response.log();
    response.statusCode(200);
    this.getLogger().info("---- Test success -----\n");

    // Wait 5 seconds for the provenance life cycle hooks to be activated
    try {
      TimeUnit.SECONDS.sleep(5);
    } catch (InterruptedException e) {
    }

    /*
     * Retrieve provenance records
     */
    this.getLogger().info("----- Retrieve provenance records -----");
    // Currently auth is not needed to see provenance records
    response = this.startRequest().log().all()
        .post(String.format("/call/?objectId=%1$s&method=getProvenanceRecords", createdId)).then();
    response.log().all();
    response.statusCode(200);
    response.assertThat().body("provenanceRecords.size()", is(2));
    response.assertThat().body("provenanceRecords",
        everyItem(hasEntry("type", "EventProvenanceRecord")));

    try {
      String prov_prefix = this.getConfig().getString("provenanceRepository.handlePrefix");
      String json_schema = TestUtils.getProvenanceRecordsJsonSchema(prov_prefix);
      response.assertThat().body(matchesJsonSchema(json_schema));
    } catch (IOException e) {
      e.printStackTrace();
    }
    this.getLogger().info("---- Test success -----\n");
  }

  @AfterAll
  public void housekeeping() {
    // We always try to delete the created test object
    if (this.createdInstanceId != null) {
      this.getLogger().info("----- DELETE -----");
      this.deleteTestInstance(this.createdInstanceId);
      this.getLogger().info("---- Test success -----\n");

      // Afterwards, clean up the prov repository
      this.getLogger().info("----- Delete provenance records -----");
      this.cleanUpProvRepository(this.createdInstanceId);
      this.getLogger().info("---- Test success -----\n");
    }
  }

  private void deleteTestInstance(String testInstanceId) {
    try {
      this.startRequestAuth().delete(String.format("/objects/%1$s", testInstanceId)).then().log()
          .all();

      // Wait 5 seconds for the provenance life cycle hooks to be activated
      TimeUnit.SECONDS.sleep(5);
    } catch (Exception e) {
      this.getLogger().info("Exception when trying to delete test object");
      e.printStackTrace();
    }
  }

  private void cleanUpProvRepository(String testInstanceId) {
    try {
      String url = this.getConfig().getString("provenanceRepository.url");
      ValidatableResponse response = this.startRequest().given().log().all()
          .get(String.format(
              "%1$s/objects?query=type:\"EventProvenanceRecord\" AND /entityId:\"%2$s\"", url,
              testInstanceId))
          .then();
      // ArrayList<HashMap> records = response.extract().path("results");
      ArrayList<HashMap<String,Object>> records = response.extract().path("results");
      for (HashMap<String,Object> record : records) {
        String recordId = (String) record.get("id");
        this.deleteProvenanceRecord(recordId);
      }
    } catch (Exception e) {
      this.getLogger().info("exception during deleteProvenanceRecordsOfTestInstance");
      e.printStackTrace();
    }
  }

  private void deleteProvenanceRecord(String id) {
    String provHostAddress = this.getConfig().getString("provenanceRepository.url");
    this.startRequestAuth().delete(String.format("%1$s/objects/%2$s", provHostAddress, id)).then()
        .statusCode(200);
    this.getLogger().info("deleted provenance record " + id);
  }
}
