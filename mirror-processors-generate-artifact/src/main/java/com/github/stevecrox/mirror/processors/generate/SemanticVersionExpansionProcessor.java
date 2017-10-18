package com.github.stevecrox.mirror.processors.generate;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.github.stevecrox.mirror.dto.Version;

/**
 * This processor extends AbstractVersionExpansionProcessor and supplies a major.minor.patch regular expression matcher (it also supports major.minor.patch). It will then generate a
 * major/minor version for the version range found within the GenerateArtifact.
 */
public final class SemanticVersionExpansionProcessor extends AbstractVersionExpansionProcessor {
  /** Semantic version regex string. */
  private static final String SEMANTIC_FORMAT = "(\\d+)\\.(\\d+)\\.(\\d+)(\\.|[-_]|\\+)?([0-9A-Za-z-.]*)?";
  /** Semantic version pattern matcher. */
  private static final Pattern SEMANTIC_PATTERN = Pattern.compile(SEMANTIC_FORMAT);

  
  public Set < String > generateVersions(final Version minVersion, final Version maxVersion) {
    final Set < String > results = new HashSet < String >();
    
    for (int major = minVersion.getMajor(); major <= maxVersion.getMajor(); major++) {
      
      final int minorStart = this.getStartPosition(major, minVersion.getMajor(), minVersion.getMinor());
      final int minorEnd = this.getEndPosition(major, maxVersion.getMajor(), maxVersion.getMinor());
      for (int minor = minorStart; minor <= minorEnd; minor++) {
        
        final int patchStart = this.getStartPosition(minor, minVersion.getMinor(), minVersion.getPatch());
        final int patchEnd;
        if (MAX_VERSION == minorEnd) {
          patchEnd = MAX_VERSION;
        } else {
          patchEnd = this.getEndPosition(minor, maxVersion.getMinor(), maxVersion.getPatch());
        }

        for (int patch = patchStart; patch <= patchEnd; patch++) {
          final Version version = new Version(major, minor, patch);
          results.add(version.toString());
          
          version.setModifierSeparator(minVersion.getModifierSeparator());
          version.setModifier(minVersion.getModifier());
          results.add(version.toString());
          
          version.setModifierSeparator(maxVersion.getModifierSeparator());
          version.setModifier(maxVersion.getModifier());
          results.add(version.toString());
        }
      }
    }
    
    return results;
  }

  @Override
  public Pattern getPatternMatcher() {
    return SEMANTIC_PATTERN;
  }
}
