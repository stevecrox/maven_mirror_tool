package com.github.stevecrox.mirror.interfaces;

public interface PackageArtifact {

  /**
   * The packaging type, linked to the type of file being downloaded.
   * @return a valid packaging type
   */
  String getPackagingType();

  /**
   * Is this Packaging object a manifest which contains download information.
   * @return true if it is
   */
  boolean isManifest();
  
  
  /**
   * Alternate Packaging type, e.g. a POM might also contain JAR files.
   * 
   * @return a valid packaging type
   */
  PackageArtifact[] getAlternatePackagingTypes();
  
  /**
   * The classifiers/modifiers associated with this type. e.g. sources is a common modifier for the JAR type.
   * 
   * @return a series of modifiers
   */
  String[] getModifiers();
}
