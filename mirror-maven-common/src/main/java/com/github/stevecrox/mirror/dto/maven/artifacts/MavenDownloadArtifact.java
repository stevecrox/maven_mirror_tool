package com.github.stevecrox.mirror.dto.maven.artifacts;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import com.github.stevecrox.mirror.constants.CharConstants;
import com.github.stevecrox.mirror.constants.StringConstants;
import com.github.stevecrox.mirror.constants.maven.MavenConstants;
import com.github.stevecrox.mirror.dto.maven.AbstractMavenArtifact;
import com.github.stevecrox.mirror.dto.maven.MavenPackaging;
import com.github.stevecrox.mirror.exceptions.DownloadArtifactException;
import com.github.stevecrox.mirror.interfaces.DownloadArtifact;
import com.github.stevecrox.mirror.interfaces.PackageArtifact;

public final class MavenDownloadArtifact extends AbstractMavenArtifact implements DownloadArtifact {

    /** Auto generated serialisation identifier. */
    private static final long serialVersionUID = 9188470380893657317L;
    /** Modifier to ensure the instances hash is unique. */
    private static final int HASH_VALUE = 97;

    private URI source;

    private URI destination;

    public MavenDownloadArtifact() {
        this(StringConstants.EMPTY, StringConstants.EMPTY, StringConstants.EMPTY, MavenPackaging.POM);
    }

    public MavenDownloadArtifact(final String group, final String artifact, final String version, final PackageArtifact pom) {
        this(group, artifact, version, StringConstants.EMPTY, pom);
    }

    public MavenDownloadArtifact(final String group, final String artifact, final String version, final String modifier, final PackageArtifact pom) {
        super(group, artifact, version, modifier, pom);

        this.source = null;
        this.destination = null;
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
     *             if artifact or packaging is null and/or empty, the assumption is the version has been checked before this is called.
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

    public URI getSource() {
        return this.source;
    }

    public void setSource(final URI uri) {
        this.source = uri;
    }

    public URI getDestination() {
        return this.destination;
    }

    public void setDestination(URI uri) {
        this.destination = uri;

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

        if (super.equals(value) && value instanceof MavenDownloadArtifact) {
            final MavenDownloadArtifact toCompare = (MavenDownloadArtifact) value;
            // check the modifiers, group, source and destination are identical.
            final boolean dst = null == this.getDestination() ? null == toCompare.getDestination() : this.getDestination() == toCompare.getDestination();
            final boolean src = null == this.getSource() ? null == toCompare.getSource() : this.getSource().equals(toCompare.getSource());

            // confirm everything is identical
            result = dst && src;
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
        result += null == this.getDestination() ? HASH_VALUE : this.getDestination().hashCode();
        result += null == this.getSource() ? HASH_VALUE : this.getSource().hashCode();

        return result;
    }

    /**
     * This method will create a complete clone of the base class.
     * 
     * @throws CloneNotSupportedException
     *             thrown if the JDK has an issue cloning the object.
     * @return a valid clone of this obect.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        final Object result = super.clone();

        if (result instanceof MavenDownloadArtifact) {
            final MavenDownloadArtifact clone = (MavenDownloadArtifact) result;
            // Clone the URI's if they exist
            if (null != this.getDestination()) {
                clone.setDestination(URI.create(this.getDestination().toString()));
            }
            if (null != this.getSource()) {
                clone.setDestination(URI.create(this.getSource().toString()));
            }
        } else {
            throw new CloneNotSupportedException("Incorrect base type created.");
        }

        return result;
    }
}
