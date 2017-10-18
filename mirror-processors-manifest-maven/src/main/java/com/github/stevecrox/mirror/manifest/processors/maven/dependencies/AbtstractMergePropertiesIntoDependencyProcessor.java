package com.github.stevecrox.mirror.manifest.processors.maven.dependencies;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.Processor;
import org.codehaus.plexus.util.StringUtils;

import com.github.stevecrox.mirror.constants.maven.MavenConstants;
import com.github.stevecrox.mirror.dto.maven.MavenDependency;

public abstract class AbtstractMergePropertiesIntoDependencyProcessor implements Processor {

      
  protected Map<String,Map<String,MavenDependency>> process(final Map<String,Map<String,MavenDependency>> mergedDependency, final Map<String, String> properties) {
    final Map<String,Map<String,MavenDependency>> result = new HashMap<String,Map<String,MavenDependency>>();
    
    // if we have valid properties and a dependency section
    if (null != properties && null != mergedDependency) {
      // cycle through the supplied dependencies and merge in the properties values.
      for (final Entry < String, Map<String, MavenDependency> > entry : mergedDependency.entrySet()) {
        final Map<String,MavenDependency> mergedArtifacts = new HashMap<String,MavenDependency>(); 
        // Ensure the group id isn't a property value
        final String mergedGroupId = this.retrieveMavenProperty(entry.getKey(), properties);
        // cycle the artifacts
        final Map<String,MavenDependency> artifacts = entry.getValue();
        for (final Entry <String, MavenDependency> artifactEntry : artifacts.entrySet()) {
          //merge and update the key
          final String mergedArtifactId = this.retrieveMavenProperty(artifactEntry.getKey(), properties);
          final MavenDependency dependency = this.retrieveMavenDependency(artifactEntry.getValue(), properties);           
          mergedArtifacts.put(mergedArtifactId, dependency);
        }
        // merging the converted values into the results.
        result.put(mergedGroupId, mergedArtifacts);
      }
    }
    
    return result;
  }
  
  private MavenDependency retrieveMavenDependency(final MavenDependency dependency, final Map<String, String> properties) {
    final String classifier = this.retrieveMavenProperty(dependency.getClassifier(), properties);
    dependency.setClassifier(classifier);
    
    final String packaging = this.retrieveMavenProperty(dependency.getPackaging(), properties);
    dependency.setPackaging(packaging);
    
    final String version = this.retrieveMavenProperty(dependency.getVersion(), properties);
    dependency.setVersion(version);
    
    return dependency;
  }
  
  private String retrieveMavenProperty(final String property, final Map<String, String> properties) {
    
    final String result;
    
    if (null != property && property.startsWith(MavenConstants.MAVEN_PROPERTIES_PREFIX) && property.endsWith(MavenConstants.MAVEN_PROPERTIES_SUFFIX)) {
      // 
      final int startPos = MavenConstants.MAVEN_PROPERTIES_PREFIX.length();
      final int endPos = property.length() - MavenConstants.MAVEN_PROPERTIES_SUFFIX.length();
      //
      final String key = property.substring(startPos, endPos);
      
      final String value = properties.get(key);
      result = StringUtils.isBlank(value) ? property : value;
    } else {
      result = property;
    }
    
    return result;
  }
}
