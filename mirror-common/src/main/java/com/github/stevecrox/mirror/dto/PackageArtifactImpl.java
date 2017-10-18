package com.github.stevecrox.mirror.dto;

import java.io.Serializable;
import java.util.Arrays;

import com.github.stevecrox.mirror.constants.StringConstants;
import com.github.stevecrox.mirror.interfaces.PackageArtifact;

/**
 * File extension associated with a specific download artifact.
 */
public final class PackageArtifactImpl implements PackageArtifact, Serializable {

  /** Auto generated serialisation identifier. */
  private static final long serialVersionUID = -8024686074428119741L;

  /** Modifier to ensure the instances hash is unique. */
  private static final int HASH_VALUE = 101;

  /** Additional Modifiers which can be applied to this object. **/
  private final String[] modifiers;

  /** Some packaging types spawn different download objects. */
  private final PackageArtifact[] packagingTypes;

  /** Is this a manifest type. */
  private final boolean manifest;

  /** The type of file to be downloaded. */
  private final String packagingType;

  /**
   * Default constructor sets everything to empty/false.
   */
  public PackageArtifactImpl() {
    this(StringConstants.EMPTY, false);
  }

  /**
   * Constructor sets everything to empty/false.
   * 
   * @param type
   *          the packaging type (e.g. the file extension)
   * @param man
   *          is this packaging type a manifest file type (e.g. packaging.json, pom fle, etc..)
   */
  public PackageArtifactImpl(final String type, final boolean man) {
    this(type, man, new String[0]);
  }

  /**
   * Constructor sets everything to empty/false.
   * 
   * @param type
   *          the packaging type (e.g. the file extension)
   * @param man
   *          is this packaging type a manifest file type (e.g. packaging.json, pom fle, etc..)
   * @param mutatations
   *          packages can have additional modifiers e.g. <artifactid>-sources.jar, this is the list of modifiers associated with this type.
   */
  public PackageArtifactImpl(final String type, final boolean man, final String[] mutatations) {
    this(type, man, mutatations, new PackageArtifact[0]);
  }

  /**
   * Constructor sets everything to empty/false.
   * 
   * @param type
   *          the packaging type (e.g. the file extension)
   * @param man
   *          is this packaging type a manifest file type (e.g. packaging.json, pom fle, etc..)
   * @param alternates
   *          Alternate file extensions associated with this one, for example the maven type: maven-plugin is anouther way of describing the JAR type.
   */
  public PackageArtifactImpl(final String type, final boolean man, final PackageArtifact[] alternates) {
    this(type, man, new String[0], alternates);
  }

  /**
   * Constructor sets everything to empty/false.
   * 
   * @param type
   *          the packaging type (e.g. the file extension)
   * @param man
   *          is this packaging type a manifest file type (e.g. packaging.json, pom fle, etc..)
   * @param mutatations
   *          packages can have additional modifiers e.g. <artifactid>-sources.jar, this is the list of modifiers associated with this type.
   * @param alternates
   *          Alternate file extensions associated with this one, for example the maven type: maven-plugin is anouther way of describing the JAR type.
   */
  public PackageArtifactImpl(final String type, final boolean man, final String[] mutatations, final PackageArtifact[] alternates) {
    super();

    if (null == mutatations || 0 == mutatations.length) {
      this.modifiers = new String[0];
    } else {
      this.modifiers = Arrays.copyOf(mutatations, mutatations.length);
    }

    if (null == alternates || 0 == alternates.length) {
      this.packagingTypes = new PackageArtifact[0];
    } else {
      this.packagingTypes = Arrays.copyOf(alternates, alternates.length);
    }

    this.manifest = man;
    this.packagingType = type;
  }

  /**
   * The packaging type, linked to the type of file being downloaded.
   * 
   * @return a valid packaging type
   */
  public String getPackagingType() {
    return this.packagingType;
  }

  /**
   * The packaging type, linked to the type of file being downloaded.
   * 
   * @return a valid packaging type
   */
  public PackageArtifact[] getAlternatePackagingTypes() {
    return this.packagingTypes;
  }

  /**
   * The classifiers/modifiers associated with this type. e.g. sources is a common modifier for the JAR type.
   * 
   * @return a series of modifiers
   */
  public String[] getModifiers() {
    return this.modifiers;
  }

  /**
   * Is this Packaging object a manifest which contains download information?
   * 
   * @return true if it is
   */
  public boolean isManifest() {
    return this.manifest;
  }

  /**
   * Should the artifact be mutated, typically only occur's on manifest.
   * 
   * @return true if various additional types should be generated off of this.
   */
  public boolean isMutatable() {
    return (null == this.modifiers ? false : this.modifiers.length > 0) || (null == this.packagingTypes ? false : this.packagingTypes.length > 0);
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

    if (value instanceof PackageArtifact) {
      final PackageArtifact toCompare = (PackageArtifact) value;
      // check the modifiers are identifier
      final boolean pack = null == this.getPackagingType() ? null == toCompare.getPackagingType() : this.getPackagingType().equals(toCompare.getPackagingType());

      final boolean man = this.isManifest() == toCompare.isManifest();
      final boolean mods = Arrays.deepEquals(this.getModifiers(), toCompare.getModifiers());
      final boolean alt = Arrays.deepEquals(this.getAlternatePackagingTypes(), toCompare.getAlternatePackagingTypes());

      // confirm everything is identical
      result = pack && man && mods && alt;
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
    int result = super.hashCode();

    result += null == this.getPackagingType() ? HASH_VALUE : this.getPackagingType().hashCode();
    result += Boolean.hashCode(this.isManifest());
    result += Arrays.hashCode(this.getAlternatePackagingTypes());
    result += Arrays.hashCode(this.getModifiers());

    return result;
  }

  /**
   * Returns just the packaging type.
   * @return only returns the packaging type as a string.
   */
  @Override
  public String toString() {
    return this.getPackagingType();
  }
}
