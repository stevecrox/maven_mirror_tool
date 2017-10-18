package com.github.stevecrox.mirror.dto;

import java.io.Serializable;

import com.github.stevecrox.mirror.interfaces.PackageArtifact;

/**
 * Base object, which is extended to produce the various types of objects the processors will handle.
 */
public abstract class AbstractBaseArtifactDTO implements Serializable, Cloneable {

  /** Auto generated serialisation identifier. */
  private static final long serialVersionUID = -8582562033297069420L;
  /** Modifier to ensure the instances hash is unique. */
  private static final int HASH_VALUE = 101;

  /** The artifact Identifier associated with the object. */
  private String artifactId;
  
  /** the version of the artifact we are retrieving. */
  private String version;

  /** Sets the downloadable packaging type. */
  private PackageArtifact packaging;

  /**
   * Constructor, sets the internal objects to the supplied parameter.
   * 
   * @param artifact
   *          The artifact Identifier associated with the object.
   * @param versioning
   *          the version of the artifact we are retrieving
   * @param type
   *          the type of file being downloaded.
   */
  public AbstractBaseArtifactDTO(final String artifact, final String versioning, final PackageArtifact type) {
    super();

    this.artifactId = artifact;
    this.version = versioning;
    this.packaging = type;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public void setArtifactId(final String artifactId) {
    this.artifactId = artifactId;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(final String version) {
    this.version = version;
  }

  public PackageArtifact getPackaging() {
    return packaging;
  }

  public void setPackaging(final PackageArtifact packaging) {
    this.packaging = packaging;
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

    if (value instanceof AbstractBaseArtifactDTO) {
      final AbstractBaseArtifactDTO toCompare = (AbstractBaseArtifactDTO) value;
      // check the modifiers are identifier
      final boolean art = null == this.getArtifactId() ? null == toCompare.getArtifactId() : this.getArtifactId().equals(toCompare.getArtifactId());
      final boolean ver = null == this.getVersion() ? null == toCompare.getVersion() : this.getVersion().equals(toCompare.getVersion());
      final boolean pac = null == this.getPackaging() ? null == toCompare.getPackaging() : this.getPackaging() == toCompare.getPackaging();

      // confirm everything is identical
      result = art && ver && pac;
    } else {
      result = false;
    }

    return result;
  }

  /**
   * Generates a unique hash code.
   * 
   * @return a hash representing this object.
   */
  @Override
  public int hashCode() {
    int result = null == this.getArtifactId() ? HASH_VALUE : this.getArtifactId().hashCode();
    result += null == this.getVersion() ? HASH_VALUE : this.getVersion().hashCode();
    result += null == this.getPackaging() ? HASH_VALUE : this.getPackaging().hashCode();

    return result;
  }

  /**
   * This method will create a complete clone of the base class.
   */
  public Object clone() throws CloneNotSupportedException {
    final Object result = super.clone();

    if (result instanceof AbstractBaseArtifactDTO) {
      final AbstractBaseArtifactDTO clone = (AbstractBaseArtifactDTO) result;
      clone.setArtifactId(this.getArtifactId());
      clone.setVersion(this.getVersion());
      // We aren't going to clone this, as typically I've expect a static instantation
      clone.setPackaging(this.getPackaging());

    } else {
      throw new CloneNotSupportedException("Incorrect base type created.");
    }

    return result;
  }
}
