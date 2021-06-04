package eu.dissco.nsidr.testing;

public class Info {
  public static void main(String[] args) {
    System.out.println("This package only serves to deliver a runnable test suite for "
        + "testing the nsidr website setup. It should be called via \n "
        + "java -Dconfig.path=config.properties -DintrusiveTests=true -jar junit-platform-console-standalone.jar"
        + "-cp target/nsidr-api-tests-0.1-test-jar-with-dependencies.jar --select-package eu.dissco.nsidr.testing");
  }
}
