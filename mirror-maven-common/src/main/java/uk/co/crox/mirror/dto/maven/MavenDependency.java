package uk.co.crox.mirror.dto.maven;

public class MavenDependency {

  private String version;

  private String packaging;

  private String classifier;

  public MavenDependency(final String clazz, final String ver, final String type) {
    super();

    this.classifier = clazz;
    this.packaging = type;
    this.version = ver;
  }

  /**
   * @return the classifier
   */
  public String getClassifier() {
    return classifier;
  }

  /**
   * @param classifier
   *          the classifier to set
   */
  public void setClassifier(final String classifier) {
    this.classifier = classifier;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(final String version) {
    this.version = version;
  }

  public String getPackaging() {
    return packaging;
  }

  public void setPackaging(final String packaging) {
    this.packaging = packaging;
  }

}
