package uk.co.crox.mirror.dto;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.co.crox.mirror.interfaces.DownloadArtifact;
import uk.co.crox.mirror.interfaces.LicenseArtifact;
import uk.co.crox.mirror.interfaces.PackageArtifact;

/**
 * Base object which contains all of the common fields expected in various platforms manifests.
 */
public abstract class AbstractManifestArtifactImpl extends AbstractBaseArtifactDTO {
  /** Auto generated serialisation identifier. */
  private static final long serialVersionUID = 8937853513290424105L;

  private Map<String, String> properties;

  private Set<DownloadArtifact> dependencies;

  private Set<LicenseArtifact> licenses;

  public AbstractManifestArtifactImpl(final String artifact, final String versioning,
      final PackageArtifact type) {
    super(artifact, versioning, type);

    this.properties = new HashMap<String, String>();
    this.dependencies = new HashSet<DownloadArtifact>();
    this.licenses = new HashSet<LicenseArtifact>();
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(final Map<String, String> properties) {
    this.properties = properties;
  }

  public Set<DownloadArtifact> getDependencies() {
    return dependencies;
  }

  public void setDependencies(final Set<DownloadArtifact> dependencies) {
    this.dependencies = dependencies;
  }

  public Set<LicenseArtifact> getLicenses() {
    return licenses;
  }

  public void setLicenses(final Set<LicenseArtifact> licenses) {
    this.licenses = licenses;
  }
}
