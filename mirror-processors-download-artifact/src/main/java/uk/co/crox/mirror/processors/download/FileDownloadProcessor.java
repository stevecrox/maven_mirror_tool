package uk.co.crox.mirror.processors.download;

import java.io.File;
import java.net.URI;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.commons.io.FileUtils;

import uk.co.crox.mirror.constants.uri.URIConstants;
import uk.co.crox.mirror.exceptions.download.DownloadArtifactRouteException;
import uk.co.crox.mirror.interfaces.DownloadArtifact;

public class FileDownloadProcessor implements Processor {

  /**
   * 
   * 
   * @param exchange
   *          body contains a DownloadArtifact
   * @throws DownloadArtifactRouteException
   *           if the exchange object isn't a DownloadArtifact.
   */
  public void process(final Exchange exchange) throws Exception {
    final Message inMessage = exchange.getIn();

    //
    final Object body = inMessage.getBody();
    if (body instanceof DownloadArtifact) {
      final DownloadArtifact artifact = (DownloadArtifact) body;

      final URI destination = artifact.getDestination();
      final URI source = artifact.getSource();

      // Only URI check actual file addresses.
      if (null == source) {

      } else if (null == destination) {

      } else if (URIConstants.FILE_SCHEMA.equalsIgnoreCase(source.getScheme())) {

        if (URIConstants.FILE_SCHEMA.equalsIgnoreCase(destination.getScheme())) {
          FileUtils.copyFile(new File(source.getPath()), new File(destination.getPath()));
        }
        // TODO insert File to URL copy tool

      } else if (URIConstants.HTTP_SCHEMA.equalsIgnoreCase(source.getScheme()) || URIConstants.HTTPS_SCHEMA.equalsIgnoreCase(source.getScheme())) {

        if (URIConstants.FILE_SCHEMA.equalsIgnoreCase(destination.getScheme())) {
          FileUtils.copyURLToFile(source.toURL(), new File(destination.getPath()));
        }
        // TODO insert URL to URL copy tool
      }
    }
  }

}
