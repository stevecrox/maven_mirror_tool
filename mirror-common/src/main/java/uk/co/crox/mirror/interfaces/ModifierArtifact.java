package uk.co.crox.mirror.interfaces;

import java.io.Serializable;

/**
 * Some artifact types support the concept of modifiers which can be attached to an object, this applies them.
 */
public interface ModifierArtifact extends Serializable, Cloneable {
  
  /**
   * This method will create a complete clone of the base class.
   * 
   * @throws CloneNotSupportedException thrown if the JDK has an issue cloning the object.
   * @return a valid clone of this obect.
   */
  Object clone() throws CloneNotSupportedException;
  
  /**
   * Additional modifiers can be attached to the artifact such as the Apache Maven classifier, this will return such modifiers.
   * 
   * @return null or empty string unless a valid modifier is supplied.
   */
  String getModifier();

  /**
   * Additional modifiers can be attached to the artifact such as the Apache Maven classifier, this will return such modifiers.
   * 
   * @param classifer
   *          - the modifier to set for the object.
   */
  void setModifier(String classifer);
}
