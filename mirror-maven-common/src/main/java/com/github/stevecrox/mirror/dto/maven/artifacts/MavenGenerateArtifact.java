package com.github.stevecrox.mirror.dto.maven.artifacts;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.github.stevecrox.mirror.constants.StringConstants;
import com.github.stevecrox.mirror.dto.AbstractBaseArtifactDTO;
import com.github.stevecrox.mirror.dto.maven.MavenPackaging;
import com.github.stevecrox.mirror.exceptions.ArtifactGenerationException;
import com.github.stevecrox.mirror.interfaces.DownloadArtifact;
import com.github.stevecrox.mirror.interfaces.GenerateArtifact;
import com.github.stevecrox.mirror.interfaces.PackageArtifact;

public final class MavenGenerateArtifact extends AbstractBaseArtifactDTO implements GenerateArtifact {

    /** auto generated serialisation identifier. */
    private static final long serialVersionUID = -1688319698875591569L;
    /** Modifier to ensure the instances hash is unique. */
    private static final int HASH_VALUE = 107;
    /** Position of the group identifier. */
    private static final int GROUP_ID_POS = 0;
    /** Position of the artifact identifier. */
    private static final int ARTIFACT_ID_POS = 1;
    /** Position of the classifiers. */
    private static final int CLASSIFERS_POS = 2;
    /** Position of the Packaging field. */
    private static final int PACKAGING_POS = 3;
    /** Position of the minimum version field. */
    private static final int MIN_VERSION_POS = 4;
    /** Position of the maximum version field. */
    private static final int MAX_VERSION_POS = 5;

    private String groupId;

    private Set<String> classifers;

    private String maxVersion;

    private Set<String> versions;

    public MavenGenerateArtifact() {
        this(StringConstants.EMPTY, StringConstants.EMPTY, StringConstants.EMPTY, StringConstants.EMPTY, MavenPackaging.POM);
    }

    public MavenGenerateArtifact(final String group, final String artifact, final String minVersion, final String maximumVersion, final PackageArtifact type) {
        super(artifact, minVersion, type);

        this.groupId = group;
        this.maxVersion = maximumVersion;

        this.classifers = new HashSet<String>();
        this.versions = new HashSet<String>();
    }

    /**
     * Get the packaging position with the data objects
     * 
     * @return the position of the packaging field.
     */
    public int getPackagingPosition() {
        return PACKAGING_POS;
    }

    /**
     * Used to get internal fields, assumption is as follows:
     * 
     * 0 = groupid 1 = artifactid 2 = classifer 4 = minimum version 5 = maximum version 3 = packaging type
     * 
     * If a number greater than 5 is supplied or less than zero this will return false;
     * 
     * @param field
     *            the field number posiion to set
     * @return null if the supplied field is invalid, or a valid object
     */
    public Object getField(final int field) {

        final Object result;

        switch (field) {
        case GROUP_ID_POS:
            result = this.getGroupId();
            break;
        case ARTIFACT_ID_POS:
            result = this.getArtifactId();
            break;
        case CLASSIFERS_POS:
            result = this.getClassifers();
            break;
        case PACKAGING_POS:
            result = this.getPackaging();
            break;
        case MIN_VERSION_POS:
            result = this.getMinVersion();
            break;
        case MAX_VERSION_POS:
            result = this.getMaxVersion();
            break;
        default:
            result = null;
            break;
        }

        return result;
    }

    /**
     * Used to set internal fields, assumption is as follows:
     * 
     * 0 = groupid 1 = artifactid 2 = classifer 4 = minimum version 5 = maximum version 3 = packaging type
     * 
     * If a number greater than 5 is supplied or less than zero this will return false;
     * 
     * @param field
     *            the field number posiion to set
     * @param value
     *            the value to set that field to.
     * @return false if there was an issue setting the value.
     */
    public boolean setField(final int field, final String value) {

        boolean result = true;

        switch (field) {
        case GROUP_ID_POS:
            this.setGroupId(value);
            break;
        case ARTIFACT_ID_POS:
            this.setArtifactId(value);
            break;
        case CLASSIFERS_POS:
            // assumption is we have a space separated set of values, each of which represents a modifier.
            final String[] values = value.split(StringConstants.SPACE);
            final Set<String> modfiers = new HashSet<String>(Arrays.asList(values));
            this.setClassifers(modfiers);
            break;
        case PACKAGING_POS:
            this.setPackaging(value);
            break;
        case MIN_VERSION_POS:
            this.setMinVersion(value);
            break;
        case MAX_VERSION_POS:
            this.setMaxVersion(value);
            break;
        default:
            result = false;
            break;
        }

        return result;
    }

    /**
     * 
     * @param packaging
     */
    public void setPackaging(final String packaging) {
        for (final PackageArtifact pack : MavenPackaging.values()) {
            if (pack.getPackagingType().equalsIgnoreCase(packaging)) {
                this.setPackaging(pack);
                break;
            }
        }
    }

    public String getMinVersion() {
        return this.getVersion();
    }

    public void setMinVersion(final String version) {
        this.setVersion(version);
    }

    public String getMaxVersion() {
        return this.maxVersion;
    }

    public void setMaxVersion(final String version) {
        this.maxVersion = version;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(final String group) {
        this.groupId = group;
    }

    public Set<String> getClassifers() {
        return classifers;
    }

    public void setClassifers(final Set<String> modifiers) {
        this.classifers = modifiers;
    }

    public Set<String> getVersions() {
        return this.versions;
    }

    public void setVersion(final Set<String> versionsToGenerate) {
        this.versions = versionsToGenerate;
    }

    /**
     * This will use the information provided to generate all possible combinations of artifact we need to download.
     * 
     * @return an empty string if there is nothing to generate
     * @throws ArtifactGenerationException
     */
    public Set<DownloadArtifact> generateArtifacts() throws ArtifactGenerationException {
        final Set<DownloadArtifact> results = new HashSet<DownloadArtifact>();

        if (null == this.getVersions() || this.getVersions().isEmpty()) {
            throw new ArtifactGenerationException("No versions were supplied, so unable to generate:\t" + this.toString());
        } else if (null == this.getClassifers() || this.getClassifers().isEmpty()) {
            // if there are noclassifiers, just generate version strings.
            for (final String version : this.getVersions()) {
                results.add(new MavenDownloadArtifact(this.getGroupId(), this.getArtifactId(), version, StringConstants.EMPTY, MavenPackaging.POM));
            }
        } else {
            // we need to generate a version for each classifier.
            for (final String classifier : this.getClassifers()) {
                for (final String version : this.getVersions()) {
                    results.add(new MavenDownloadArtifact(this.getGroupId(), this.getArtifactId(), version, classifier, MavenPackaging.POM));
                }
            }
        }

        return results;
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

        if (value instanceof MavenGenerateArtifact && super.equals(value)) {
            final MavenGenerateArtifact toCompare = (MavenGenerateArtifact) value;
            // check the modifiers are identifier
            final boolean grp = null == this.getGroupId() ? null == toCompare.getGroupId() : this.getGroupId().equals(toCompare.getGroupId());
            final boolean ver = null == this.getMaxVersion() ? null == toCompare.getMaxVersion() : this.getMaxVersion().equals(toCompare.getMaxVersion());
            // Compare the lists
            final boolean clas = null == this.getClassifers() ? null == toCompare.getClassifers() : this.getClassifers().equals(toCompare.getClassifers());
            final boolean vers = null == this.getVersions() ? null == toCompare.getVersions() : this.getVersions().equals(toCompare.getVersions());

            // confirm everything is identical
            result = grp && ver && clas && vers;
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

        result += null == this.getGroupId() ? HASH_VALUE : this.getGroupId().hashCode();
        result += null == this.getMaxVersion() ? HASH_VALUE : this.getMaxVersion().hashCode();
        result += null == this.getClassifers() ? HASH_VALUE : this.getClassifers().hashCode();
        result += null == this.getVersions() ? HASH_VALUE : this.getVersions().hashCode();

        return result;
    }
}
