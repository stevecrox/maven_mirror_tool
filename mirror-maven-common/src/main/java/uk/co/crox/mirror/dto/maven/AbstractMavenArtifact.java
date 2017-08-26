package uk.co.crox.mirror.dto.maven;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import uk.co.crox.mirror.constants.CharConstants;
import uk.co.crox.mirror.constants.StringConstants;
import uk.co.crox.mirror.constants.maven.MavenConstants;
import uk.co.crox.mirror.dto.AbstractBaseArtifactDTO;
import uk.co.crox.mirror.exceptions.DownloadArtifactException;
import uk.co.crox.mirror.interfaces.DownloadArtifact;
import uk.co.crox.mirror.interfaces.ModifierArtifact;
import uk.co.crox.mirror.interfaces.PackageArtifact;

public abstract class AbstractMavenArtifact extends AbstractBaseArtifactDTO implements ModifierArtifact {

  /** Auto generated serialisation identifier. */
  private static final long serialVersionUID = 9188470380893657317L;
  /** Modifier to ensure the instances hash is unique. */
  private static final int HASH_VALUE = 97;
  /** Apache Maven group id representation. */
  private String groupId;
  /** Variable used to hold an Apache Maven classifier string. **/
  private String modifier;

  public AbstractMavenArtifact() {
    this(StringConstants.EMPTY, StringConstants.EMPTY, StringConstants.EMPTY, MavenPackaging.POM);
  }

  public AbstractMavenArtifact(final String group, final String artifact, final String version, final PackageArtifact pom) {
    this(group, artifact, version, StringConstants.EMPTY, pom);
  }

  public AbstractMavenArtifact(final String group, final String artifact, final String version, final String modifier, final PackageArtifact pom) {
    super(artifact, version, pom);

    this.groupId = group;
    this.setModifier(modifier);
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(final String group) {
    this.groupId = group;
  }

  @Override
  public String toString() {
    final StringBuilder result = new StringBuilder();

    result.append(this.getGroupId());
    result.append(StringConstants.COMMA);
    result.append(this.getArtifactId());
    result.append(StringConstants.COMMA);

    if (null != this.getModifier() && !this.getModifier().trim().isEmpty()) {
      result.append(this.getModifier());
      result.append(StringConstants.COMMA);
    }

    result.append(this.getVersion());
    result.append(StringConstants.COMMA);
    result.append(this.getPackaging());

    return result.toString();
  }

  /**
   * Additional modifiers can be attached to the artifact such as the Apache Maven classifier, this will return such modifiers.
   * 
   * @return null or empty string unless a valid modifier is supplied.
   */
  public String getModifier() {
    return modifier;
  }

  /**
   * Additional modifiers can be attached to the artifact such as the Apache Maven classifier, this will return such modifiers.
   * 
   * @param classifer
   *          - the modifier to set for the object.
   */
  public void setModifier(final String classifer) {
    this.modifier = classifer;
  }

  /**
   * 
   * @return
   */
  public Set<URI> generateSources() throws DownloadArtifactException {
    final Set<URI> result = new HashSet<URI>();

    for (final URI repository : MavenConstants.getRepositories()) {
      result.add(this.generateDestination(repository));
    }

    return result;
  }

  public URI generateDestination(final URI target) throws DownloadArtifactException {

    final char separator;
    if (null == target) {
      throw new DownloadArtifactException();
    } else if (MavenConstants.FILE_SCHEMA.equalsIgnoreCase(target.getScheme())) {
      separator = File.separatorChar;
    } else if (MavenConstants.HTTP_SCHEMA.equalsIgnoreCase(target.getScheme())) {
      separator = CharConstants.FORWARD_SLASH;
    } else if (MavenConstants.HTTPS_SCHEMA.equalsIgnoreCase(target.getScheme())) {
      separator = CharConstants.FORWARD_SLASH;
    } else {
      throw new DownloadArtifactException();
    }

    //
    final String path = this.generateFilePath(separator);
    final String prefix = target.toString();

    final String uri;
    if (prefix.endsWith(StringConstants.EMPTY + separator)) {
      uri = prefix + path;
    } else {
      uri = prefix + separator + path;
    }

    return URI.create(uri.replaceAll(StringConstants.SPACE, "%20"));
  }

  private String generateFilePath(final char separator) throws DownloadArtifactException {
    if (null == this.getGroupId() || this.getGroupId().trim().isEmpty()) {
      throw new DownloadArtifactException("Unable to generate filename no group identifier for:\t" + this.toString());
    } else if (null == this.getVersion() || this.getVersion().trim().isEmpty()) {
      throw new DownloadArtifactException("Unable to generate filename no version for:\t" + this.toString());
    }

    final StringBuilder path = new StringBuilder();
    path.append(this.getGroupId().replace(CharConstants.PERIOD, separator));
    path.append(separator);
    path.append(this.getArtifactId());
    path.append(separator);
    path.append(this.getVersion());
    path.append(separator);
    path.append(this.generateFileName());

    return path.toString();
  }

  /**
   * This method generates a Maven object's file name, this follows a standard structure of <artifact id>-<version>.<packaging>.
   * 
   * @return a valid string conforming to the above.
   * @throws DownloadArtifactException
   *           if artifact or packaging is null and/or empty, the assumption is the version has been checked before this is called.
   */
  private String generateFileName() throws DownloadArtifactException {
    final StringBuilder result = new StringBuilder();

    if (null == this.getArtifactId() || this.getArtifactId().trim().isEmpty()) {
      throw new DownloadArtifactException("Unable to generate filename no artifact for:\t" + this.toString());
    } else if (null == this.getPackaging()) {
      throw new DownloadArtifactException("Unable to generate filename no packaging for:\t" + this.toString());
    }

    result.append(this.getArtifactId().trim());
    result.append(StringConstants.HYPHEN);
    result.append(this.getVersion().trim());

    // Only add a
    if (null != this.getModifier() && !this.getModifier().trim().isEmpty()) {
      result.append(StringConstants.HYPHEN);
      result.append(this.getModifier().trim());
    }

    result.append(StringConstants.PERIOD);
    result.append(this.getPackaging().getPackagingType());

    return result.toString();
  }

  /**
   * Compares the supplied parameter with this one, checks it is a verison object contains the same major, minor, patch, modifier and modifier separator.
   * 
   * @param value
   *          object to compare this one to.
   * @return false if the supplied object isn't identical.
   */
  @Override
  public boolean equals(final Object value) {
    final boolean result;

    if (super.equals(value) && value instanceof AbstractMavenArtifact) {
      final AbstractMavenArtifact toCompare = (AbstractMavenArtifact) value;
      // check the modifiers, group, source and destination are identical.
      final boolean grp = null == this.getGroupId() ? null == toCompare.getGroupId() : this.getGroupId().equals(toCompare.getGroupId());
      final boolean mod = null == this.getModifier() ? null == toCompare.getModifier() : this.getModifier().equals(toCompare.getModifier());
      // confirm everything is identical
      result = grp && mod;
    } else {
      result = false;
    }

    return result;
  }

  /**
   * Generates a unique hash code, uses the base classes as well as group id, modifier and the destintation/source objects.
   * 
   * @return a hash representing this object.
   */
  @Override
  public int hashCode() {
    int result = super.hashCode();
    result += null == this.getGroupId() ? HASH_VALUE : this.getGroupId().hashCode();
    result += null == this.getModifier() ? HASH_VALUE : this.getModifier().hashCode();

    return result;
  }

  /**
   * This method will create a complete clone of the base class.
   * 
   * @throws CloneNotSupportedException
   *           thrown if the JDK has an issue cloning the object.
   * @return a valid clone of this obect.
   */
  @Override
  public Object clone() throws CloneNotSupportedException {
    final Object result = super.clone();

    if (result instanceof AbstractMavenArtifact) {
      final AbstractMavenArtifact clone = (AbstractMavenArtifact) result;
      clone.setGroupId(this.getGroupId());
      clone.setModifier(this.getModifier());
    } else {
      throw new CloneNotSupportedException("Incorrect base type created.");
    }

    return result;
  }
}
