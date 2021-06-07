package eu.dissco.nsidr.testing;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

import eu.dissco.doec.digitalObjectRepository.DigitalObjectRepositoryException;
import eu.dissco.doec.digitalObjectRepository.DigitalObjectRepositoryClient;
import eu.dissco.doec.digitalObjectRepository.DigitalObjectRepositoryInfo;

import net.dona.doip.InDoipSegment;
import net.dona.doip.InDoipMessage;
import net.dona.doip.client.DigitalObject;
import net.dona.doip.client.transport.DoipClientResponse;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntrusiveDoipTests extends DoipTests {
  // To-Do: definedInstanceIdSuffix script currently contains a hardcoded id,
  // because this is necessary for doec for lifecycle actions. Should be
  // replaced by using generic returned ID in the future.
  public final String definedInstanceIdSuffix = "test_ods_instance_doip";
  private String createdInstanceId;

  @Test
  @EnabledIfSystemProperty(named = "intrusiveTests", matches = "true")
  public void ODS_crud_operations_test_DOIP() throws DigitalObjectRepositoryException {
    this.getLogger().info("##### starting ODS_crud_operations_test via DIOP #####\n");
    this.getLogger().info("----- CREATE -----");
    /*
     * CREATE { "id": "{{ cordra.cordra_nsidr.prefix }}/test_ods_instance_doip", "midslevel": 0,
     * "institutionCode": ["CU"], "physicalSpecimenId": "test_physicalSpecimenId", "scientificName":
     * "test_scientificName" }
     */
    String prefix = this.getConfig().getString("digitalObjectRepository.handlePrefix");
    DigitalObject testObject = new DigitalObject();
    testObject.id = String.format("%1$s/%2$s", prefix, this.definedInstanceIdSuffix);
    testObject.type = "DigitalSpecimen";
    JsonObject content = new JsonObject();
    content.addProperty("midslevel", 0);
    JsonArray institutionCodes = new JsonArray();
    institutionCodes.add("CU");
    content.add("institutionCode", institutionCodes);
    content.addProperty("physicalSpecimenId", "test_physicalSpecimenId");
    content.addProperty("scientificName", "test_scientificName");
    testObject.setAttribute("content", content);
    DigitalObject createdObject = this.getDoipClient().create(testObject);
    // To-Do: Find correct method to assert createdObject
    // assertThat(created, equalTo(testObject));
    this.createdInstanceId = createdObject.id;
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
    content.addProperty("scientificName", "test_changed_physicalSpecimenId");
    testObject.setAttribute("content", content);
    testObject.id = createdObject.id;
    DigitalObject updatedObject = this.getDoipClient().update(testObject);
    // To-Do: Find correct method to assert createdObject
    assertThat(
        updatedObject.attributes.getAsJsonObject("content").get("scientificName").getAsString(),
        equalTo("test_changed_physicalSpecimenId"));
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
    JsonObject emptyAttributes = new JsonObject();
    String targetId = createdObject.id;
    String operationId = "getProvenanceRecords";
    DoipClientResponse response =
        this.getDoipClient().performOperation(targetId, operationId, emptyAttributes);
    assertThat(response.getStatus(), equalTo("0.DOIP/Status.001"));
    this.getLogger().info("---- Test success -----\n");

    InDoipMessage output = response.getOutput();

    for (InDoipSegment segment : output) {
      if (segment.isJson()) {
        try {
          JsonElement outputJson = segment.getJson();
          this.getLogger().info("Result provenance records" + outputJson.toString());
          JsonArray provenanceRecords =
              outputJson.getAsJsonObject().getAsJsonArray("provenanceRecords");
          assertThat(provenanceRecords.size(), is(2));
          String prov_prefix = this.getConfig().getString("provenanceRepository.handlePrefix");
          String json_schema = TestUtils.getProvenanceRecordsJsonSchema(prov_prefix);
          assertThat(outputJson.toString(), matchesJsonSchema(json_schema));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
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
      this.getDoipClient().delete(this.createdInstanceId);
      // Wait 5 seconds for the provenance life cycle hooks to be
      // activated
      TimeUnit.SECONDS.sleep(5);
    } catch (Exception e) {
      this.getLogger().info("Exception when trying to delete test object");
      e.printStackTrace();
    }
  }

  private void cleanUpProvRepository(String testInstanceId) {
    try {
      String url = this.getConfig().getString("provenanceRepository.url");
      int doipPort = this.getConfig().getInt("provenanceRepository.doipPort");
      String handlePrefix = this.getConfig().getString("provenanceRepository.handlePrefix");
      String username = this.getConfig().getString("provenanceRepository.username");
      String password = this.getConfig().getString("provenanceRepository.password");
      int pageSize = this.getConfig().getInt("provenanceRepository.searchPageSize");
      Boolean validateCerts = this.getConfig().getBoolean("cert_validation");
      if((password == null || password.isEmpty()) && !validateCerts){
        // if the password is not provided we set credentials to null to prevent auth checking
        username = null;
        password = null;
      }
      DigitalObjectRepositoryInfo info = new DigitalObjectRepositoryInfo(url, doipPort,
          handlePrefix, username, password, pageSize);
      DigitalObjectRepositoryClient provRepo = new DigitalObjectRepositoryClient(info);

      String query =
          String.format("type:\"EventProvenanceRecord\" AND /entityId:\"%1$s\"", testInstanceId);
      List<DigitalObject> searchResults = provRepo.searchAll(query);
      for (DigitalObject record : searchResults) {
        provRepo.delete(record.id);
        this.getLogger().info("deleted provenance record " + record.id);
      }
      provRepo.close();
    } catch (Exception e) {
      this.getLogger().info("exception during deleteProvenanceRecordsOfTestInstance");
      e.printStackTrace();
    }
  }
}
