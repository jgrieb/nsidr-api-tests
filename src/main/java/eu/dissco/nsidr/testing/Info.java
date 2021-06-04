package eu.dissco.nsidr.testing;

public class Info {
  public static void main(String[] args) {
    System.out.println("This package only serves to deliver a runnable test suite for "
        + "testing the nsidr website setup. It should be called via \n "
        + "java -Dserver.host=CORDRA_HOST -jar junit-platform-console-standalone.jar "
        + "-cp nsidr-test-suite-0.1-test-jar-with-dependencies.jar --select-package eu.dissco.testing");
  }
}
