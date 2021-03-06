package com.github.stevecrox.mirror.manifest.processors.maven.dependencies;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;

import com.github.stevecrox.mirror.dto.maven.MavenDependency;
import com.github.stevecrox.mirror.dto.maven.artifacts.MavenManifestArtifact;
import com.github.stevecrox.mirror.manifest.exceptions.maven.MavenManifestRouteException;

public final class MergePropertiesIntoDependencyManagementProcessor extends AbtstractMergePropertiesIntoDependencyProcessor {  
  
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
      
      final Map<String,Map<String,MavenDependency>> mergedDependency = artifact.getDependencyManagement();
      // if we have valid properties and a management section
      if (null != properties && null != mergedDependency) {
        final Map<String,Map<String,MavenDependency>> updated = this.process(mergedDependency, properties);
        artifact.setDependencyManagement(updated);
      }      
    } else {
      throw new MavenManifestRouteException("Unexpected Artifact was supplied.");
    }
  }
}
