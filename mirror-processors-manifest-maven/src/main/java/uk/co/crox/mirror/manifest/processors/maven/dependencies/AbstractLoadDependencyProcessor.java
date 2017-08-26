package uk.co.crox.mirror.manifest.processors.maven.dependencies;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import uk.co.crox.mirror.dto.maven.MavenDependency;
import uk.co.crox.mirror.dto.maven.MavenPackaging;
import uk.co.crox.mirror.dto.maven.artifacts.MavenDownloadArtifact;
import uk.co.crox.mirror.dto.maven.artifacts.MavenManifestArtifact;
import uk.co.crox.mirror.exceptions.DownloadArtifactException;
import uk.co.crox.mirror.exceptions.url.URLCheckerException;
import uk.co.crox.mirror.interfaces.DownloadArtifact;
import uk.co.crox.mirror.manifest.exceptions.maven.ManifestParseException;
import uk.co.crox.mirror.manifest.exceptions.maven.MavenManifestRouteException;
import uk.co.crox.mirror.manifest.exceptions.maven.MissingManifestException;
import uk.co.crox.mirror.processors.download.AbstractGenerateURIProcessor;

/**
 * 
 */
public abstract class AbstractLoadDependencyProcessor extends AbstractGenerateURIProcessor {

  /** The target repository to check the dependencies out into. */
  private final URI destination;

  /**
   * Constructor.
   * 
   * @param target
   * @throws DownloadArtifactRouteException
   */
  public AbstractLoadDependencyProcessor(final URI target) throws MavenManifestRouteException {
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

  /**
   * This will process the supplied MavenManifestArtifact and extract all of the dependency management.
   * @param exchange in message should contain a MavenManifestArtifact
   */
  public void process(final Exchange exchange) throws Exception {
    final Message inMessage = exchange.getIn();

    //
    final Object body = inMessage.getBody();
    if (body instanceof MavenManifestArtifact) {
      final MavenManifestArtifact artifact = (MavenManifestArtifact) body;

      // generate a parent object for us to process.
      final MavenDownloadArtifact downloadArtifact = new MavenDownloadArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), MavenPackaging.PARENT_POM);
      final URI path = downloadArtifact.generateDestination(this.getDestination());
      downloadArtifact.setDestination(path);
      
      //
      final Map<String,Map<String,MavenDependency>>  dependencies = this.loadParent(downloadArtifact);
      artifact.addDependencyManagement(dependencies);

    } else {
      throw new MavenManifestRouteException("Unexpected Artifact was supplied.");
    }

  }

  protected abstract void processModel(final Map<String,Map<String,MavenDependency>> result, final Model model);
  
  /**
   * 
   * @param artifact
   * @return
   * @throws MissingManifestException
   * @throws ManifestParseException
   */
  private Map<String,Map<String,MavenDependency>> loadParent(final DownloadArtifact artifact) throws MissingManifestException, ManifestParseException {
    
    final Map<String,Map<String,MavenDependency>> result = new HashMap<String,Map<String,MavenDependency>>();
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
      if (null != parent) {
        // generate a parent object for us to process.
        final MavenDownloadArtifact parentArt = new MavenDownloadArtifact(parent.getGroupId(), parent.getArtifactId(), parent.getVersion(), MavenPackaging.PARENT_POM);
        final URI parentURI = parentArt.generateDestination(this.getDestination());
        parentArt.setDestination(parentURI);
        // process to create our head manifest, Maven works through inheritance so we have to
        // goto the highest level and work our way down.
        result.putAll(this.loadParent(parentArt));
      }
      
      //
      this.processModel(result, model);  

    } catch (FileNotFoundException|XmlPullParserException|EOFException e) {
      throw new MissingManifestException();
    } catch (DownloadArtifactException | URLCheckerException | IOException e) {
      throw new ManifestParseException(e);
    } finally {
      IOUtils.closeQuietly(input);
    }

    return result;
  }
  
  /**
   * This will convert the s
   * @param dependencies
   * @param toProcess
   */
  protected void loadDependencies(final Map<String,Map<String,MavenDependency>> dependencies, final List<Dependency> toProcess) {
    
    if (null != toProcess) {
      for (final Dependency dependency : toProcess) {
        // Only merge if we have a valid 
        if (StringUtils.isNotBlank(dependency.getGroupId())
            && StringUtils.isNotBlank(dependency.getArtifactId())) {
          
          // retrieve everything we've captured
          Map<String,MavenDependency> group = dependencies.get(dependency.getGroupId());
          if (null == group) {
            group = new HashMap<String,MavenDependency>();            
            dependencies.put(dependency.getGroupId(), group);
          }
          
          // 
          MavenDependency artifact = group.get(dependency.getArtifactId());
          if (null == artifact) {
            artifact = new MavenDependency(dependency.getClassifier(), dependency.getVersion(), dependency.getType());
            group.put(dependency.getArtifactId(), artifact);
          } else {
            this.updateDependency(artifact, dependency);
          }
        }
      }
    }
  }
  
  private void updateDependency(final MavenDependency artifact, final Dependency dependency) {
    if (StringUtils.isNotBlank(dependency.getClassifier())) {
      artifact.setClassifier(dependency.getClassifier());
    }
    
    if (StringUtils.isNotBlank(dependency.getVersion())) {
      artifact.setVersion(dependency.getVersion());
    }
    
    if (StringUtils.isNotBlank(dependency.getType())) {
      artifact.setPackaging(dependency.getType());
    }
  }
}
