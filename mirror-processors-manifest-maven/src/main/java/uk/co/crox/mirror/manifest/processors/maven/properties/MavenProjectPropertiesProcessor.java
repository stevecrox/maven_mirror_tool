package uk.co.crox.mirror.manifest.processors.maven.properties;

import java.util.Properties;

import org.apache.camel.Exchange;
import org.apache.camel.Message;

import uk.co.crox.mirror.dto.maven.artifacts.MavenManifestArtifact;
import uk.co.crox.mirror.interfaces.PackageArtifact;
import uk.co.crox.mirror.manifest.exceptions.maven.MavenManifestRouteException;
import uk.co.crox.mirror.processors.download.AbstractGenerateURIProcessor;

public final class MavenProjectPropertiesProcessor extends AbstractGenerateURIProcessor {

  /**
   *   
   */
  public void process(final Exchange exchange) throws Exception {
    final Message inMessage = exchange.getIn();
    //
    final Object body = inMessage.getBody();
    if (body instanceof MavenManifestArtifact) {
      final MavenManifestArtifact artifact = (MavenManifestArtifact) body;
      
      final Properties properties = new Properties();
      // define project specific versions
      properties.put("project.groupId", artifact.getGroupId());
      properties.put("project.artifactId", artifact.getArtifactId());
      properties.put("project.version", artifact.getVersion());
      // 
      final PackageArtifact projectPackage = artifact.getPackaging();
      if (null != projectPackage) {
        properties.put("project.packaging", projectPackage.getPackagingType());
      } else {
        properties.put("project.packaging", "pom");
      }

      
      
      // Merge in the standard properties
      artifact.addProperties(properties);
      
    } else {
      throw new MavenManifestRouteException("Unexpected Artifact was supplied.");
    }
  }
}
