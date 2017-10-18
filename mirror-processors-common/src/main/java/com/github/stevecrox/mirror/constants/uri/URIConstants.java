package com.github.stevecrox.mirror.constants.uri;

public final class URIConstants {
 
  /** Schema definition for a HTTP URL connection. */
  public static final String HTTP_SCHEMA = "http";
  
  /** Schema definition for a HTTPS URL connection. */
  public static final String HTTPS_SCHEMA = "https";
  
  /** Schema definition for a File connection. */
  public static final String FILE_SCHEMA = "file";
  
  /** HTTP GET Request. */
  public static final String HTTP_GET_REQUEST = "GET";
  
  /** Response code for a successful connection. */
  public static final int HTTP_GOOD_RESPONSE = 200;
  
  /** Response code for a missing connection. */
  public static final int HTTP_MISSING_RESPONSE = 404;
  
  /**
   * Unused Utility constructor.
   */
  private URIConstants() {
    super();
  }
}
