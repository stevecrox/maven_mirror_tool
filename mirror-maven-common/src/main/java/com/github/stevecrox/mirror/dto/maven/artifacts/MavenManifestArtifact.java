package com.github.stevecrox.mirror.dto.maven.artifacts;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.github.stevecrox.mirror.constants.StringConstants;
import com.github.stevecrox.mirror.dto.maven.AbstractMavenArtifact;
import com.github.stevecrox.mirror.dto.maven.MavenDependency;
import com.github.stevecrox.mirror.dto.maven.MavenPackaging;
import com.github.stevecrox.mirror.interfaces.DownloadArtifact;
import com.github.stevecrox.mirror.interfaces.PackageArtifact;
import com.github.stevecrox.mirror.maven.interfaces.ManifestArtifact;

public final class MavenManifestArtifact extends AbstractMavenArtifact implements ManifestArtifact {

    /** Auto generated serialisation identifier. */
    private static final long serialVersionUID = 1192656141209075988L;

    private Map<String, String> properties;

    private Map<String, Map<String, MavenDependency>> dependencyManagement;

    private Map<String, Map<String, MavenDependency>> dependencies;

    public MavenManifestArtifact() {
        this(StringConstants.EMPTY, StringConstants.EMPTY, StringConstants.EMPTY, MavenPackaging.POM);
    }

    public MavenManifestArtifact(final String group, final String artifact, final String version, final PackageArtifact pom) {
        this(group, artifact, version, StringConstants.EMPTY, pom);
    }

    public MavenManifestArtifact(final String group, final String artifact, final String version, final String modifier, final PackageArtifact pom) {
        super(group, artifact, version, modifier, pom);

        this.setProperties(new ConcurrentHashMap<String, String>());
        this.setDependencyManagement(new HashMap<String, Map<String, MavenDependency>>());
        this.setDependencies(new HashMap<String, Map<String, MavenDependency>>());
    }

    public void addDependencies(final Map<String, Map<String, MavenDependency>> deps) {

        if (null != deps) {

            if (null == this.dependencies) {
                this.dependencies = new HashMap<String, Map<String, MavenDependency>>();
            }

            this.dependencies.putAll(deps);
        }
    }

    public void addDependencyManagement(final Map<String, Map<String, MavenDependency>> management) {

        if (null != management) {

            if (null == this.dependencyManagement) {
                this.dependencyManagement = new HashMap<String, Map<String, MavenDependency>>();
            }

            this.dependencyManagement.putAll(management);
        }
    }

    /**
     * @param properties
     *            the properties to set
     */
    public void addProperties(final Properties properties) {

        if (null != properties) {
            for (Entry<Object, Object> entry : properties.entrySet()) {

                if (entry.getKey() instanceof String && entry.getValue() instanceof String) {
                    this.properties.put((String) entry.getKey(), (String) entry.getValue());
                }
            }
        }
    }

    @Override
    public Set<DownloadArtifact> getDependencyArtifacts() {
        return null;
    }

    public Map<String, Map<String, MavenDependency>> getDependencyManagement() {
        return dependencyManagement;
    }

    /**
     * @return the properties
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * @param dependencies
     *            the dependencies to set
     */
    public void setDependencies(final Map<String, Map<String, MavenDependency>> dependencies) {
        this.dependencies = dependencies;
    }

    /**
     * @return the dependencies
     */
    public Map<String, Map<String, MavenDependency>> getDependencies() {
        return this.dependencies;
    }

    public void setDependencyManagement(final Map<String, Map<String, MavenDependency>> management) {
        this.dependencyManagement = management;
    }

    /**
     * @param properties
     *            the properties to set
     */
    public void setProperties(final Map<String, String> properties) {
        this.properties = properties;
    }
}
