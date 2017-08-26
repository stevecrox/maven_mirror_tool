package uk.co.crox.mirror.processors.download;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;

import org.apache.camel.Processor;

import uk.co.crox.mirror.constants.uri.URIConstants;
import uk.co.crox.mirror.exceptions.url.URLCheckerException;

public abstract class AbstractGenerateURIProcessor implements Processor {

  protected boolean fileExists(final URI source) {
    boolean result = false;
    // Only URI check actual file addresses.
    if (null != source && URIConstants.FILE_SCHEMA.equalsIgnoreCase(source.getScheme())) {
      final File checkFile = new File(source.getPath());
      result = checkFile.exists();
    }

    return result;
  }
  
  /**
   * This will convert the URI into a URL and will then try to establish a HTTP connection to it, if a connection can be established then this will return
   * the URI which established the connection. If there are multiple URI's which exist this will return the first one found.
   * 
   * @param source a URI to convert into a URL
   * @return false if no connection could be made.
   */
  protected boolean urlExists(final URI source) throws URLCheckerException {
    boolean result = false;
    // Only URL check actual URL addresses.
    if (null != source && (URIConstants.HTTP_SCHEMA.equalsIgnoreCase(source.getScheme()) || URIConstants.HTTPS_SCHEMA.equalsIgnoreCase(source.getScheme()))) {
      
      HttpURLConnection connection = null;
      try {
        final URL downloadURL = source.toURL();
        // open a URL connection
        connection = (HttpURLConnection) downloadURL.openConnection();
        connection.setRequestMethod(URIConstants.HTTP_GET_REQUEST);
        connection.connect();
        // Confirm the file exists
        final int code = connection.getResponseCode();
        // If we receive a 200 response that indicates the pom exists.
        result = URIConstants.HTTP_GOOD_RESPONSE == code;
      } catch (final MalformedURLException e) {
        throw new URLCheckerException("Issue converting to URL:\t" + source, e);
      } catch (ProtocolException e) {
        throw new URLCheckerException("Issue cinnecting to URL:\t" + source, e);
      } catch (IOException e) {
        throw new URLCheckerException("Issue testing URL:\t" + source, e);
      } finally {
        if (null != connection) {
          connection.disconnect();
        }
      }
    }

    return result;
  }
}
