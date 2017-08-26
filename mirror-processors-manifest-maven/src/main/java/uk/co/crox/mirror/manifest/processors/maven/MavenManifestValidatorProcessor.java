package uk.co.crox.mirror.manifest.processors.maven;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import uk.co.crox.mirror.dto.maven.MavenPackaging;
import uk.co.crox.mirror.interfaces.DownloadArtifact;
import uk.co.crox.mirror.manifest.exceptions.maven.MavenManifestRouteException;

public class MavenManifestValidatorProcessor implements Processor {

  /**
   * 
   * 
   * @param exchange
   *          body contains a DownloadArtifact
   * @throws MavenManifestRouteException
   *           if the exchange object isn't a DownloadArtifact.
   */
  public void process(final Exchange exchange) throws Exception {
    final Message inMessage = exchange.getIn();

    //
    final Object body = inMessage.getBody();
    if (body instanceof DownloadArtifact) {
      final DownloadArtifact artifact = (DownloadArtifact) body;

      if (!MavenPackaging.POM.equals(artifact.getPackaging())) {
        throw new MavenManifestRouteException("Invalid Packaging type was supplied.");
      }
    } else {
      throw new MavenManifestRouteException("Unexpected Artifact was supplied.");
    }
  }

}
