package uk.co.crox.mirror.manifest.processors.maven.properties;

import java.util.Properties;

import org.apache.camel.Exchange;
import org.apache.camel.Message;

import uk.co.crox.mirror.manifest.exceptions.maven.MavenManifestRouteException;
import uk.co.crox.mirror.maven.interfaces.ManifestArtifact;
import uk.co.crox.mirror.processors.download.AbstractGenerateURIProcessor;

public final class Maven3PropertiesProcessor extends AbstractGenerateURIProcessor {

  /**
   *   
   */
  public void process(final Exchange exchange) throws Exception {
    final Message inMessage = exchange.getIn();
    //
    final Object body = inMessage.getBody();
    if (body instanceof ManifestArtifact) {
      final ManifestArtifact artifact = (ManifestArtifact) body;
      
      final Properties properties = new Properties();
      // define Maven specific versions
      properties.put("doxiaVersion", "1.6");
      properties.put("doxia-sitetoolsVersion", "1.6");
      properties.put("mavenVersion", "3.3.3");
      properties.put("mavenArchiverVersion", "3.1.1");
      properties.put("mavenPluginPluginVersion", "3.2");
      properties.put("mavenFilteringVersion", "3.1.1");
      properties.put("pmdVersion", "3.7");
      properties.put("scmVersion", "1.9.5");
      properties.put("wagonVersion", "1.0");
      // Commons-io
      properties.put("commons.clirr.version", "2.4");
      // Merge in the standard properties
      artifact.addProperties(properties);
      
    } else {
      throw new MavenManifestRouteException("Unexpected Artifact was supplied.");
    }
  }
}
