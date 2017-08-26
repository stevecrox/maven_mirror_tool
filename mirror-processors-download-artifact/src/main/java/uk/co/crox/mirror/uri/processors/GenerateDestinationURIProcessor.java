package uk.co.crox.mirror.uri.processors;

import java.net.URI;

import org.apache.camel.Exchange;
import org.apache.camel.Message;

import uk.co.crox.mirror.exceptions.download.DestinationExistsException;
import uk.co.crox.mirror.exceptions.download.DownloadArtifactRouteException;
import uk.co.crox.mirror.exceptions.url.GenerateDestinationURIException;
import uk.co.crox.mirror.interfaces.DownloadArtifact;
import uk.co.crox.mirror.processors.download.AbstractGenerateURIProcessor;

/**
 * This class will take in an GenerateArtifact and ask it to generate all of the required DownloadArtifacts and return those.
 */
public final class GenerateDestinationURIProcessor extends AbstractGenerateURIProcessor {

  /** The target repository to check the dependencies out into. */
  private final URI destination;
  
  /**
   * Constructor.
   * @param target
   * @throws DownloadArtifactRouteException 
   */
  public GenerateDestinationURIProcessor(final URI target) throws DownloadArtifactRouteException {
    super();
    
    if (null == target) {
      throw new DownloadArtifactRouteException("Supplied URI was invalid.");
    }
    
    this.destination = target;
  }
  
  /**
   * This function will take in an GenerateArtifact and ask it to generate all of the required DownloadArtifacts and return those.
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

      final URI destination;
      if(null == artifact.getDestination()) {
        // Generate the destination as it doesn't currently exist in the object.
        destination = artifact.generateDestination(this.getDestination());
      } else {
        destination = artifact.getDestination();
      }
      
      // Ensure the destination doesn't exist
      if (null == destination) {
        throw new GenerateDestinationURIException("No Destination paths were returned for artifact:\t" + artifact);
      } else if (this.urlExists(destination)) {
        throw new DestinationExistsException("Path was a pre-existing URL:\t" + destination);
      } else if (this.fileExists(destination)) {
        throw new DestinationExistsException("Path was a pre-existing File:\t" + destination);
      }
      
      artifact.setDestination(destination);

    } else {
      throw new DownloadArtifactRouteException("Invalid message object was supplied");
    }
  }

  /**
   * Retrieves the target repository to check the dependencies out into.
   * @return a valid URI location.
   */
  public URI getDestination() {
    return destination;
  }
}
