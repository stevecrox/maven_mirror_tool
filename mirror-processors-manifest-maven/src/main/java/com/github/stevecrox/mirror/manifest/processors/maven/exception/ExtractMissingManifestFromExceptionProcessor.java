package com.github.stevecrox.mirror.manifest.processors.maven.exception;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import com.github.stevecrox.mirror.dto.maven.MavenPackaging;
import com.github.stevecrox.mirror.dto.maven.artifacts.MavenDownloadArtifact;
import com.github.stevecrox.mirror.exceptions.DownloadArtifactException;
import com.github.stevecrox.mirror.exceptions.url.URLCheckerException;
import com.github.stevecrox.mirror.interfaces.DownloadArtifact;
import com.github.stevecrox.mirror.manifest.exceptions.maven.ManifestParseException;
import com.github.stevecrox.mirror.manifest.exceptions.maven.MavenManifestRouteException;
import com.github.stevecrox.mirror.manifest.exceptions.maven.MissingManifestException;
import com.github.stevecrox.mirror.processors.download.AbstractGenerateURIProcessor;

public class ExtractMissingManifestFromExceptionProcessor extends AbstractGenerateURIProcessor {

  /** The target repository to check the dependencies out into. */
  private final URI destination;

  /**
   * Constructor.
   * 
   * @param target
   * @throws DownloadArtifactRouteException
   */
  public ExtractMissingManifestFromExceptionProcessor(final URI target) throws MavenManifestRouteException {
    super();

    if (null == target) {
      throw new MavenManifestRouteException("Supplied URI was invalid.");
    }

    this.destination = target;
  }

  /**
   * Retrieves the target repository to check the dependencies out into.
   * 
   * @return a valid URI location.
   */
  public URI getDestination() {
    return destination;
  }

  @Override
  public void process(final Exchange exchange) throws Exception {
    final Message inMessage = exchange.getIn();
    final Message outMessage = exchange.getOut();

    // Extracts out the missing manifest.
    final Object body = inMessage.getBody();
    if (body instanceof DownloadArtifact) {
      final DownloadArtifact artifact = (DownloadArtifact) body;
      final DownloadArtifact toDownload = this.loadParent(artifact);

      if (null == toDownload) {
        throw new MavenManifestRouteException("Nothing to retrieve.");
      }
      
      outMessage.setBody(toDownload);
    } else {
      throw new MavenManifestRouteException("Unexpected Artifact was supplied.");
    }
  }

  /**
   * 
   * @param source
   * @param artifact
   * @return
   * @throws MissingManifestException
   * @throws ManifestParseException
   */
  private DownloadArtifact loadParent(final DownloadArtifact artifact) throws MissingManifestException, ManifestParseException {
    DownloadArtifact result = null;
    InputStream input = null;

    try {
      if (this.fileExists(artifact.getDestination()) || this.urlExists(artifact.getDestination())) {
        final URL urlDestination = artifact.getDestination().toURL();
        input = urlDestination.openStream();

        final MavenXpp3Reader reader = new MavenXpp3Reader();
        final Model model = reader.read(input);
        final Parent parent = model.getParent();

        // If no parent we don't need to keep loading we can just use the information in the POM.
        if (null != parent) {
          // generate a parent object for us to process.
          final MavenDownloadArtifact parentArt = new MavenDownloadArtifact(parent.getGroupId(), parent.getArtifactId(), parent.getVersion(), MavenPackaging.PARENT_POM);
          final URI parentURI = parentArt.generateDestination(this.getDestination());
          parentArt.setDestination(parentURI);

          // process to create our head manifest, Maven works through inheritance so we have to
          // goto the highest level and work our way down.
          final DownloadArtifact pomArtifact = this.loadParent(parentArt);
          
          if (null == pomArtifact) {
            result = parentArt;
          } else {
            result = pomArtifact;
          }
        }
      }
    } catch (DownloadArtifactException | URLCheckerException | XmlPullParserException | IOException e) {
      // LOGGER
    } finally {
      IOUtils.closeQuietly(input);
    }

    return result;
  }

}
