package uk.co.crox.mirror.uri.processors;

import java.net.URI;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.camel.Message;

import uk.co.crox.mirror.exceptions.ArtifactGenerationException;
import uk.co.crox.mirror.exceptions.DownloadArtifactException;
import uk.co.crox.mirror.exceptions.download.DownloadArtifactRouteException;
import uk.co.crox.mirror.interfaces.DownloadArtifact;
import uk.co.crox.mirror.processors.download.AbstractGenerateURIProcessor;

/**
 * This class will take in an GenerateArtifact and ask it to generate all of the required DownloadArtifacts and return those.
 */
public final class GenerateSourceURIProcessor extends AbstractGenerateURIProcessor {

  /**
   * This function will take in an GenerateArtifact and ask it to generate all of the required DownloadArtifacts and return those.
   * 
   * @param exchange
   *          body contains a GenerateArtifact
   * @throws DownloadArtifactRouteException
   *           if the exchange object isn't a GenerateArtifact.
   * @throws ArtifactGenerationException
   *           if there is an issue generating versions.
   */
  public void process(final Exchange exchange) throws Exception {
    final Message inMessage = exchange.getIn();

    //
    final Object body = inMessage.getBody();
    if (body instanceof DownloadArtifact) {
      final DownloadArtifact artifact = (DownloadArtifact) body;

      URI downloadURI = null;
      if (null == artifact.getSource()) {
        // Generate the sources
        final Set<URI> sources = artifact.generateSources();
        if (null == sources || sources.isEmpty()) {
          throw new DownloadArtifactException("No Sources were returned for artifact:\t" + artifact);
        }

        // iterate over all sources and find a valid retrival path
        for (final URI source : sources) {
          // Check the supplied URL exists if it does use this source to retrieve from.
          if (this.urlExists(source)) {
            downloadURI = source;
            break;
            // Check the file exists.
          } else if (this.fileExists(source)) {
            downloadURI = source;
            break;
          }
        }
      } else if (this.urlExists(artifact.getSource()) || this.fileExists(artifact.getSource())) {
        // as the stored value is fine just return that.
        downloadURI = artifact.getSource();
      }

      if (null == downloadURI) {
        throw new DownloadArtifactException("No Source could be found for:\t" + artifact);
      }

      artifact.setSource(downloadURI);
    } else {
      throw new DownloadArtifactRouteException("Invalid message object was supplied");
    }
  }
}
