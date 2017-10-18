package com.github.stevecrox.mirror.dto.maven;

import java.io.Serializable;

public class MavenDependency implements Serializable, Cloneable {

    /** auto generated serialisation identifier. */
    private static final long serialVersionUID = -9217941284660237435L;

    /** Modifier to ensure the instances hash is unique. */
    private static final int HASH_VALUE = 103;
    
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
     * Compares the supplied parameter with this one, checks it is a verison object contains the same major, minor, patch, modifier and modifier separator.
     * 
     * @param value
     *            object to compare this one to.
     * @return false if the supplied object isn't identical.
     */
    @Override
    public boolean equals(final Object value) {
        final boolean result;

        if (super.equals(value) && value instanceof MavenDependency) {
            final MavenDependency toCompare = (MavenDependency) value;
            // check the modifiers, group, source and destination are identical.
            final boolean cls = null == this.getClassifier() ? null == toCompare.getClassifier() : this.getClassifier() == toCompare.getClassifier();
            final boolean pak = null == this.getPackaging() ? null == toCompare.getPackaging() : this.getPackaging().equals(toCompare.getPackaging());
            final boolean ver = null == this.getVersion() ? null == toCompare.getVersion() : this.getVersion().equals(toCompare.getVersion());
            
            // confirm everything is identical
            result = cls && pak && ver;
        } else {
            result = false;
        }

        return result;
    }
    
    /**
     * @return the classifier
     */
    public String getClassifier() {
        return classifier;
    }

    /**
     * @param classifier
     *            the classifier to set
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
    
    /**
     * Generates a unique hash code, uses the base classes as well as group id, modifier and the destintation/source objects.
     * 
     * @return a hash representing this object.
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result += null == this.getClassifier() ? HASH_VALUE : this.getClassifier().hashCode();
        result += null == this.getPackaging() ? HASH_VALUE : this.getPackaging().hashCode();
        result += null == this.getVersion() ? HASH_VALUE : this.getVersion().hashCode();
        
        return result;
    }

}
