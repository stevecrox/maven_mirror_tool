package uk.co.crox.mirror.manifest.processors.maven.dependencies;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.codehaus.plexus.util.StringUtils;

import uk.co.crox.mirror.dto.maven.MavenDependency;
import uk.co.crox.mirror.dto.maven.artifacts.MavenManifestArtifact;
import uk.co.crox.mirror.manifest.exceptions.maven.MavenManifestRouteException;

public final class MergeDependencyManagementIntoDependencies implements Processor {

  @Override
  public void process(final Exchange exchange) throws Exception {
    final Message inMessage = exchange.getIn();
    //
    final Object body = inMessage.getBody();
    if (body instanceof MavenManifestArtifact) {
      final MavenManifestArtifact artifact = (MavenManifestArtifact) body;
      
      final Map<String,Map<String,MavenDependency>> management = artifact.getDependencyManagement();
      final Map<String,Map<String,MavenDependency>> dependencies = artifact.getDependencies();
      
      // Cycle through the dependencies to get to the packaging, version and classifier values held against them
      // these are the ones most likely the ones be updated. Dependencies are held in a group-> artifact approach
      for (final Entry<String,Map<String,MavenDependency>> groupEntry : dependencies.entrySet()) {
        final Map<String,MavenDependency> groupValue = groupEntry.getValue();
        // cycle through all artifacts stored against this group and merge in the dependency management information
        // where fields are empty.
        for (final Entry<String,MavenDependency> artifactEntry : groupValue.entrySet()) {
          
          if(this.isComplete(artifactEntry.getValue())) {
            this.mergeDependencyManagement(groupEntry.getKey(), artifactEntry.getKey(), artifactEntry.getValue(), management);
          }
        }
      }
    } else {
      throw new MavenManifestRouteException("Unexpected Artifact was supplied.");
    }

  }

  private boolean isComplete(final MavenDependency dependency) {
    final boolean result;
    if (null == dependency) {
      result = false;
    } else {
      result = StringUtils.isNotBlank(dependency.getClassifier()) && StringUtils.isNotBlank(dependency.getPackaging()) && StringUtils.isNotBlank(dependency.getVersion());
    }
    
    return result;
  }
  
  
  private void mergeDependencyManagement(final String groupId, final String artifactId, final MavenDependency dependency, final Map<String,Map<String,MavenDependency>> management) {
    
    if (null != dependency && null != management) {
      final Map<String,MavenDependency> groupValue = management.get(groupId);
      if (null != groupValue) {
        
        final MavenDependency managedDependency = groupValue.get(artifactId);
        if (null != managedDependency) {
          
          if(StringUtils.isBlank(dependency.getClassifier())) {
            dependency.setClassifier(managedDependency.getClassifier());
          }
          
          if(StringUtils.isBlank(dependency.getPackaging())) {
            dependency.setPackaging(managedDependency.getPackaging());
          }
          
          if(StringUtils.isBlank(dependency.getVersion())) {
            dependency.setVersion(managedDependency.getVersion());
          }
        }
      }
    }
    
  }
}
