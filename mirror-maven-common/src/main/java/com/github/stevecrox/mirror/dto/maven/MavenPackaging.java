package com.github.stevecrox.mirror.dto.maven;

import java.util.ArrayList;
import java.util.List;

import com.github.stevecrox.mirror.constants.StringConstants;
import com.github.stevecrox.mirror.dto.PackageArtifactImpl;
import com.github.stevecrox.mirror.interfaces.PackageArtifact;

public final class MavenPackaging {

    private static final String[] PARENT_MUTATIONS = new String[] { "site" };

    private static final String[] ZIP_MUTATIONS = new String[] { "source-release" };

    private static final String[] STANDARD_MUTATIONS = new String[] { StringConstants.EMPTY, "aop", "noaop", "nodeps", "no aop", "javadoc", "javadocs", "source", "sources",
            "test-javadoc", "test-javadocs", "test-source", "test-sources", "tests-javadoc", "tests-javadocs", "tests-source", "tests-sources" };

    public static final PackageArtifactImpl ZIP = new PackageArtifactImpl("zip", false, ZIP_MUTATIONS);

    public static final PackageArtifactImpl XML = new PackageArtifactImpl("xml", false, PARENT_MUTATIONS);

    public static final PackageArtifactImpl JAR = new PackageArtifactImpl("jar", false, STANDARD_MUTATIONS, new PackageArtifact[] { ZIP });

    public static final PackageArtifactImpl POM = new PackageArtifactImpl("pom", true, new PackageArtifact[] { JAR, XML });

    public static final PackageArtifactImpl PARENT_POM = new PackageArtifactImpl("pom", true, new PackageArtifact[] { XML });

    public static final PackageArtifactImpl MAVEN_PLUGIN = new PackageArtifactImpl("maven-plugin", false, new PackageArtifact[] { JAR });

    public static final PackageArtifactImpl WAR = new PackageArtifactImpl("war", false, STANDARD_MUTATIONS);

    public static final PackageArtifactImpl EAR = new PackageArtifactImpl("ear", false, STANDARD_MUTATIONS);

    public static final PackageArtifactImpl NAR = new PackageArtifactImpl("nar", false, STANDARD_MUTATIONS);

    public static final PackageArtifactImpl EJB = new PackageArtifactImpl("ejb", false, STANDARD_MUTATIONS);

    public static final PackageArtifactImpl ELB_CLIENT = new PackageArtifactImpl("ejb-client", false, STANDARD_MUTATIONS);

    public static PackageArtifact getPackageArtifact(final String packaging) {
        PackageArtifact result = null;

        if (null != packaging && packaging.trim().length() > 0) {
            for (final PackageArtifact artifact : values()) {
                if (artifact.getPackagingType().equals(packaging)) {
                    result = artifact;
                }
            }
        }

        return result;
    }

    public static PackageArtifact[] values() {
        final List<PackageArtifact> result = new ArrayList<PackageArtifact>();

        result.add(MavenPackaging.EAR);
        result.add(MavenPackaging.EJB);
        result.add(MavenPackaging.ELB_CLIENT);
        result.add(MavenPackaging.JAR);
        result.add(MavenPackaging.MAVEN_PLUGIN);
        result.add(MavenPackaging.NAR);
        result.add(MavenPackaging.PARENT_POM);
        result.add(MavenPackaging.POM);
        result.add(MavenPackaging.WAR);

        return result.toArray(new PackageArtifact[result.size()]);
    }

    /**
     * Unused Utility class constructor.
     */
    private MavenPackaging() {
        super();
    }
}
