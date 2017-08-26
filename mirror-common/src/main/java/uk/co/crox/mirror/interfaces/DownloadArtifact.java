package uk.co.crox.mirror.interfaces;

import java.io.Serializable;
import java.net.URI;
import java.util.Set;

import uk.co.crox.mirror.exceptions.DownloadArtifactException;

public interface DownloadArtifact  extends Serializable, Cloneable {
	
  /**
   * This method will create a complete clone of the base class.
   * 
   * @throws CloneNotSupportedException thrown if the JDK has an issue cloning the object.
   * @return a valid clone of this obect.
   */
  Object clone() throws CloneNotSupportedException;
  
  URI getSource();
  
  void setSource(final URI uri);
  
	Set<URI> generateSources() throws DownloadArtifactException;
	
	URI getDestination();
  
  void setDestination(final URI uri);
	
	URI generateDestination(final URI target) throws DownloadArtifactException;

  PackageArtifact getPackaging();

  void setPackaging(final PackageArtifact packaging);
}
