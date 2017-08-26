package uk.co.crox.mirror.maven.interfaces;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import uk.co.crox.mirror.interfaces.DownloadArtifact;

public interface ManifestArtifact {
  /**
   * @return the properties
   */
  Map<String, String> getProperties();

  /**
   * @param properties
   *          the properties to set
   */
  void addProperties(Properties properties);

  /**
   * @param properties
   *          the properties to set
   */
  void setProperties(Map<String, String> properties);
  
  /**
   * @return the dependencies
   */
  Set<DownloadArtifact> getDependencyArtifacts();
}
