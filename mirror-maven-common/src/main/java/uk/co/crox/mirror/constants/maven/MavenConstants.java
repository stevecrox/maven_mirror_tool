package uk.co.crox.mirror.constants.maven;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public final class MavenConstants {

  /** Maven properties prefix. */
  public static final String MAVEN_PROPERTIES_PREFIX = "${";
  /** Maven properties prefix. */
  public static final String MAVEN_PROPERTIES_SUFFIX = "}";
  
  /** Schema definition for a HTTP URL connection. */
  public static final String HTTP_SCHEMA = "http";
  
  /** Schema definition for a HTTPS URL connection. */
  public static final String HTTPS_SCHEMA = "https";
  
  /** Schema definition for a File connection. */
  public static final String FILE_SCHEMA = "file";
  
  /** The URL for Maven Central. */
  private static final String MAVEN_CENTRAL = "http://repo1.maven.org/maven2/";


  public static List<URI> getRepositories() {
    final List<URI> results = new ArrayList<URI>();
    
    final URI central = URI.create(MAVEN_CENTRAL);
    results.add(central);
    
    return results;
  }

  /** Unused utility constructor. */
  private MavenConstants() {
    super();
  }
}
