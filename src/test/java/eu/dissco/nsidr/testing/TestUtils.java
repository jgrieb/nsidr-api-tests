package eu.dissco.nsidr.testing;

import eu.dissco.doec.utils.FileUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;

import org.slf4j.LoggerFactory;

class TestUtils {
  public static final String schemaName = "getProvenanceRecords_schema.json";

  static Configuration loadTestConfig() throws ConfigurationException {
    String configFilePath = System.getProperty("config.path");
    if (configFilePath == null || configFilePath == "") {
      throw new IllegalArgumentException(
          "You must provide the config.path property as in '-Dconfig.path=PATH_TO_FILE'");
    }
    try {
      return FileUtils.loadConfigurationFromFilePath(configFilePath);
    } catch (ConfigurationException e) {
      LoggerFactory.getLogger(TestUtils.class).info("Configuration file could not be loaded");
      throw e;
    }
  }

  static String getProvenanceRecordsJsonSchema(String provenanceRepositoryHandlePrefix)
      throws IOException {
    InputStream is = TestUtils.class.getClassLoader().getResourceAsStream(schemaName);
    InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
    BufferedReader reader = new BufferedReader(streamReader);
    String line;
    String text = "";
    while ((line = reader.readLine()) != null) {
      text += line;
    }
    String json_schema = text.replace("{%cordra_prov_prefix%}", provenanceRepositoryHandlePrefix);
    return json_schema;
  }
}
