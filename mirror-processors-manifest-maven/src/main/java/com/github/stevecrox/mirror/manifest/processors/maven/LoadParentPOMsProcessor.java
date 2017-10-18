package com.github.stevecrox.mirror.manifest.processors.maven;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import com.github.stevecrox.mirror.dto.maven.MavenPackaging;
import com.github.stevecrox.mirror.dto.maven.artifacts.MavenDownloadArtifact;
import com.github.stevecrox.mirror.dto.maven.artifacts.MavenManifestArtifact;
import com.github.stevecrox.mirror.exceptions.DownloadArtifactException;
import com.github.stevecrox.mirror.exceptions.url.URLCheckerException;
import com.github.stevecrox.mirror.interfaces.DownloadArtifact;
import com.github.stevecrox.mirror.interfaces.PackageArtifact;
import com.github.stevecrox.mirror.manifest.exceptions.maven.ManifestParseException;
import com.github.stevecrox.mirror.manifest.exceptions.maven.MavenManifestRouteException;
import com.github.stevecrox.mirror.manifest.exceptions.maven.MissingManifestException;
import com.github.stevecrox.mirror.maven.interfaces.ManifestArtifact;
import com.github.stevecrox.mirror.processors.download.AbstractGenerateURIProcessor;

public final class LoadParentPOMsProcessor extends AbstractGenerateURIProcessor {

  /** The target repository to check the dependencies out into. */
  private final URI destination;

  /**
   * Constructor.
   * 
   * @param target
   * @throws DownloadArtifactRouteException
   */
  public LoadParentPOMsProcessor(final URI target) throws MavenManifestRouteException {
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

  public void process(final Exchange exchange) throws Exception {
    final Message inMessage = exchange.getIn();
    final Message outMessage = exchange.getOut();

    //
    final Object body = inMessage.getBody();
    if (body instanceof DownloadArtifact) {
      final DownloadArtifact artifact = (DownloadArtifact) body;

      if (null == artifact.getDestination()) {
        final URI path = artifact.generateDestination(this.getDestination());
        artifact.setDestination(path);
      }
      
      final ManifestArtifact manifest = this.loadParent(artifact, artifact);
      outMessage.setBody(manifest);
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
  private MavenManifestArtifact loadParent(final DownloadArtifact source, final DownloadArtifact artifact) throws MissingManifestException, ManifestParseException {
    MavenManifestArtifact manifest = null;
    InputStream input = null;

    try {
      if (this.fileExists(artifact.getDestination()) || this.urlExists(artifact.getDestination())) {
        final URL urlDestination = artifact.getDestination().toURL();
        input = urlDestination.openStream();
      }

      // if we couldn't open a stream it is because the file doesn't exist and we need to retrieve it.
      if (null == input) {
        throw new MissingManifestException();
      }

      final MavenXpp3Reader reader = new MavenXpp3Reader();
      final Model model = reader.read(input);
      final Parent parent = model.getParent();

      // If no parent we don't need to keep loading we can just use the information in the POM.
      if (null == parent) {
        // assume the manifest is the head parent POM and set up the configuration for it.
        manifest = new MavenManifestArtifact();
        manifest.setPackaging(MavenPackaging.PARENT_POM);
        // merge this level into the manifest
        this.updateManifest(model, manifest);
      } else {
        // generate a parent object for us to process.
        final MavenDownloadArtifact parentArt = new MavenDownloadArtifact(parent.getGroupId(), parent.getArtifactId(), parent.getVersion(), MavenPackaging.PARENT_POM);
        final URI parentURI = parentArt.generateDestination(this.getDestination());
        parentArt.setDestination(parentURI);
        // process to create our head manifest, Maven works through inheritance so we have to
        // goto the highest level and work our way down.
        manifest = this.loadParent(artifact, parentArt);
        // merge this level into the manifest
        this.updateManifest(model, manifest);
      }

    } catch (FileNotFoundException|XmlPullParserException|EOFException e) {
      throw new MissingManifestException();
    } catch (DownloadArtifactException | URLCheckerException | IOException e) {
      throw new ManifestParseException(e);
    } finally {
      IOUtils.closeQuietly(input);
    }

    return manifest;
  }

  private void updateManifest(final Model model, final MavenManifestArtifact manifest) {
    // override the group id if the package has a different one.
    if (StringUtils.isNotBlank(model.getGroupId())) {
      manifest.setGroupId(model.getGroupId());
    }
    // Each POM should have it's own artifact Id (we only expect them to be similar if the group id has changed.)
    if (StringUtils.isNotBlank(model.getArtifactId())) {
      manifest.setArtifactId(model.getArtifactId());
    }

    // Version is probably inherited, but may be different dependency on the open source structure.
    if (StringUtils.isNotBlank(model.getVersion())) {
      manifest.setVersion(model.getVersion());
    }

    // override the packaging if a type is explicitly listed.
    final PackageArtifact packaging = MavenPackaging.getPackageArtifact(model.getPackaging());
    if (null != packaging) {
      manifest.setPackaging(packaging);
    }
  }
}
