package uk.co.crox.mirror.manifest.processors.maven.dependencies;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;

import uk.co.crox.mirror.dto.maven.MavenDependency;
import uk.co.crox.mirror.dto.maven.artifacts.MavenManifestArtifact;
import uk.co.crox.mirror.manifest.exceptions.maven.MavenManifestRouteException;

public final class MergePropertiesIntoDependencyProcessor extends AbtstractMergePropertiesIntoDependencyProcessor {  
  
  /**
   *   
   */
  public void process(final Exchange exchange) throws Exception {
    final Message inMessage = exchange.getIn();
    //
    final Object body = inMessage.getBody();
    if (body instanceof MavenManifestArtifact) {
      final MavenManifestArtifact artifact = (MavenManifestArtifact) body;
      
      final Map<String, String> properties = artifact.getProperties();
      final Map<String,Map<String,MavenDependency>> mergedDependency = artifact.getDependencies();
      
      // if we have valid properties and a management section
      if (null != properties && null != mergedDependency) {
        final Map<String,Map<String,MavenDependency>> updated = this.process(mergedDependency, properties);
        artifact.setDependencies(updated);
      }      
    } else {
      throw new MavenManifestRouteException("Unexpected Artifact was supplied.");
    }
  }
}
