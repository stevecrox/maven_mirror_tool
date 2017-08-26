package uk.co.crox.mirror.processors.download.manifests;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import uk.co.crox.mirror.exceptions.download.NotManifestException;
import uk.co.crox.mirror.interfaces.DownloadArtifact;
import uk.co.crox.mirror.interfaces.PackageArtifact;

/**
 * Processor will throw an exception if the supplied packageartifact isn't a manifest.
 */
public final class ManifestValidatorProcessor implements Processor {

  /**
   * Processor will throw an exception if the supplied packageartifact isn't a manifest.
   * 
   * @param exchange
   *          a message containing a downloadartifact object.
   * @throws Exception
   *           only a NotManifestException is expected, all other possible exceptions should be handled.
   */
  public void process(final Exchange exchange) throws Exception {
    final Message inMessage = exchange.getIn();

    //
    final Object body = inMessage.getBody();
    if (body instanceof DownloadArtifact) {
      final DownloadArtifact artifact = (DownloadArtifact) body;
      final PackageArtifact packaging = artifact.getPackaging();

      if (null == packaging || !packaging.isManifest()) {
        throw new NotManifestException("Artifact was not a manifest:\t" + artifact.toString());
      }

    } else {
      throw new NotManifestException("Unknown artifact type was supplied.");
    }
  }
}
