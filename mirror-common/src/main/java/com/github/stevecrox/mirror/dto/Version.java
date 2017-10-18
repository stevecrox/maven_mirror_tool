package com.github.stevecrox.mirror.dto;

import java.io.Serializable;

import com.github.stevecrox.mirror.constants.StringConstants;

public final class Version implements Serializable {

  /** Auto generated serialisation identifier. */
  private static final long serialVersionUID = 6586122874302747344L;
  /** Modifier to ensure the instances hash is unique. */
  private static final int HASH_VALUE = 97;

  /** The major version for the semantic version. **/
  private int major;
  /** The minor version for the semantic version. **/
  private int minor;
  /** The patch version for the semantic version. **/
  private int patch;
  /** The modifier e.g. RELEASE. **/
  private String modifier;
  /** The modifier separator e.g. e.g. 4.1.2-RELEASE. **/
  private String modifierSeparator;

  /**
   * Constructor, sets the versions to 0 to indicate not set.
   */
  public Version() {
    this(Integer.MIN_VALUE, Integer.MIN_VALUE);
  }

  /**
   * Constructor, sets the version modifier and separator to empty and the patch to 0
   * 
   * @param majorVersion
   *          The major version for the semantic version.
   * @param minorVersion
   *          The minor version for the semantic version.
   */
  public Version(final int majorVersion, final int minorVersion) {
    this(majorVersion, minorVersion, Integer.MIN_VALUE);
  }
  
  /**
   * Constructor, sets the version modifier and separator to empty.
   * 
   * @param majorVersion
   *          The major version for the semantic version.
   * @param minorVersion
   *          The minor version for the semantic version.
   * @param patchVersion
   *          The patch version for the semantic version.
   */
  public Version(final int majorVersion, final int minorVersion, final int patchVersion) {
    this(majorVersion, minorVersion, patchVersion, StringConstants.EMPTY, StringConstants.EMPTY);
  }

  /**
   * Constructor.
   * 
   * @param majorVersion
   *          The major version for the semantic version.
   * @param minorVersion
   *          The minor version for the semantic version.
   * @param separator
   *          The modifier separator e.g. e.g. 4.1.2-RELEASE.
   * @param verModifier
   *          The modifier e.g. RELEASE.
   */
  public Version(final int majorVersion, final int minorVersion, final String separator, final String verModifier) {
    this(majorVersion, minorVersion, Integer.MIN_VALUE, separator, verModifier);
  }
  
  /**
   * Constructor.
   * 
   * @param majorVersion
   *          The major version for the semantic version.
   * @param minorVersion
   *          The minor version for the semantic version.
   * @param patchVersion
   *          The patch version for the semantic version.
   * @param separator
   *          The modifier separator e.g. e.g. 4.1.2-RELEASE.
   * @param verModifier
   *          The modifier e.g. RELEASE.
   */
  public Version(final int majorVersion, final int minorVersion, final int patchVersion, final String separator, final String verModifier) {
    super();

    this.major = majorVersion;
    this.minor = minorVersion;
    this.patch = patchVersion;
    this.modifierSeparator = separator;
    this.modifier = verModifier;
  }
  
  private int compare(final int lhs, final int rhs) {
    final int result;
    
    // compare the minor version as the major version was identical
    if (lhs > rhs) {
      result = 1;
    } else if (lhs == rhs) {
      result = 0;
    } else {
      result = -1;
    }
    
    return result;
  }
  
  /**
   * 
   * @param toCompare
   * @return
   */
  public int compare(final Version toCompare) {
    final int result;
    
    final int majorComparison = this.compare(this.getMajor(), toCompare.getMajor());
    // if the major version is identical, move on to the minor version
    if (0 == majorComparison) {
      final int minorComparison = this.compare(this.getMinor(), toCompare.getMinor());
      // if the major and minor versions are identical, move on to the patch version
      if (0 == minorComparison) {
        result = this.compare(this.getPatch(), toCompare.getPatch());
      } else {
        result = minorComparison;
      }
    } else {
      result = majorComparison;
    }
    
    return result;
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

    if (value instanceof Version) {
      final Version toCompare = (Version) value;
      final boolean version = this.getMajor() == toCompare.getMajor() && this.getMinor() == toCompare.getMinor() && this.getPatch() == toCompare.getPatch();
      // check the modifiers are identifier
      final boolean mod = null == this.getModifier() ? null == toCompare.getModifier() : this.getModifier().equals(toCompare.getModifier());
      final boolean sep = null == this.getModifierSeparator() ? null == toCompare.getModifierSeparator() : this.getModifierSeparator().equals(toCompare.getModifierSeparator());
      // confirm everything is identical
      result = version && mod && sep;
    } else {
      result = false;
    }

    return result;
  }

  /**
   * Generates a unique hash code.
   * @return a  hash representing this object.
   */
  public int hashCode() {
    int result = Integer.hashCode(this.getMajor());
    result += Integer.hashCode(this.getMinor());
    result += Integer.hashCode(this.getPatch());

    result += null == this.getModifier() ? HASH_VALUE : this.getModifier().hashCode();
    result += null == this.getModifierSeparator() ? HASH_VALUE : this.getModifierSeparator().hashCode();

    return result;
  }

  public int getMajor() {
    return major;
  }

  public void setMajor(final int major) {
    this.major = major;
  }

  public int getMinor() {
    return minor;
  }

  public void setMinor(final int minor) {
    this.minor = minor;
  }

  public int getPatch() {
    return patch;
  }

  public void setPatch(final int patch) {
    this.patch = patch;
  }

  public String getModifier() {
    return modifier;
  }

  public void setModifier(final String modifier) {
    this.modifier = modifier;
  }

  public String getModifierSeparator() {
    return modifierSeparator;
  }

  public void setModifierSeparator(final String modifierSeparator) {
    this.modifierSeparator = modifierSeparator;
  }
  
  /**
   * Recreates the version string in a form which can be used to download.
   * @return a <major>.<minor>.<patch><modifier separator><modifier>
   */
  @Override
  public String toString() {
    final StringBuilder result = new StringBuilder();
    
    result.append(Integer.toString(this.getMajor()));
    // Only add a minor version if one exists
    if (this.getMinor() >= 0) {
      result.append(StringConstants.PERIOD);
      result.append(Integer.toString(this.getMinor()));
      // Only add a patch version if a minor version exists
      if (this.getPatch() >= 0) {
        result.append(StringConstants.PERIOD);
        result.append(Integer.toString(this.getPatch()));
      }
    }
    
    if (null != this.getModifierSeparator() && !this.getModifierSeparator().trim().isEmpty()) {
      result.append(this.getModifierSeparator());
    }
    
    if (null != this.getModifier() && !this.getModifier().trim().isEmpty()) {
      result.append(this.getModifier());
    }
    
    return result.toString();
  }
}
