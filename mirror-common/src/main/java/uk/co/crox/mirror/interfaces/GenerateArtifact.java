package uk.co.crox.mirror.interfaces;

import java.io.Serializable;
import java.util.Set;

import uk.co.crox.mirror.exceptions.ArtifactGenerationException;
import uk.co.crox.mirror.interfaces.PackageArtifact;

public interface GenerateArtifact extends Serializable {

  /**
   * Get the packaging position with the data objects
   * @return the position of the packaging field.
   */
  int getPackagingPosition();
  
  /**
   * Used to get internal fields.
   * 
   * @param field
   *          the field number posiion to set
   * @return null if the supplied field is invalid, or a valid object
   */
  Object getField(int field);
  
  /**
   * Used to set internal fields, based on a numerical order.
   * 
   * @param field
   *          the field number posiion to set
   * @param value
   *          the value to set that field to.
   * @return true if the field was successful, or false.
   */
  boolean setField(int field, String value);

  PackageArtifact getPackaging();
  
  String getMinVersion();

  void setMinVersion(String version);

  String getMaxVersion();

  void setMaxVersion(String version);

  Set<String> getVersions();

  void setVersion(Set<String> versions);

  Set<DownloadArtifact> generateArtifacts() throws ArtifactGenerationException;
}
