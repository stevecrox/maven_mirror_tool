package com.github.stevecrox.mirror.processors.generate;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.github.stevecrox.mirror.dto.Version;

/**
 * This processor extends AbstractVersionExpansionProcessor and supplies a major.minor regular expression matcher (it also supports major.minor.patch). It will then generate a
 * major/minor version for the version range found within the GenerateArtifact.
 */
public final class MajorMinorVersionExpansionProcessor extends AbstractVersionExpansionProcessor {
  /** Semantic version regex string. */
  private static final String MAJOR_MINOR_FORMAT = "(\\d+)\\.(\\d+)(?:\\.)?(\\d*)(\\.|[-_]|\\+)?([0-9A-Za-z-.]*)?";
  /** Semantic version pattern matcher. */
  private static final Pattern MAJOR_MINOR_PATTERN = Pattern.compile(MAJOR_MINOR_FORMAT);

  /**
   * This is supplied two version objects (representing a range) and will generate the major.minor versions for all versions within that range. When generating the versions this
   * will create three versions, the version, the version with the min version modifier and the version with the max version modifier. A Set is used to ensure all strings held are
   * unique.
   * 
   * The method expects the min version to be less than or equal to than the max and doesn't handle the inverse position.
   * 
   * @param minVersion
   *          the minimum possible version e.g. 4.1
   * @param maxVersion
   *          the maximum possible version e.g 4.2.5-Release
   * @return an empty list if there aren't any versions.
   */
  public Set<String> generateVersions(final Version minVersion, final Version maxVersion) {
    final Set<String> results = new HashSet<String>();

    for (int major = minVersion.getMajor(); major <= maxVersion.getMajor(); major++) {

      final int minorStart = this.getStartPosition(major, minVersion.getMajor(), minVersion.getMinor());
      final int minorEnd = this.getEndPosition(major, maxVersion.getMajor(), maxVersion.getMinor());
      for (int minor = minorStart; minor <= minorEnd; minor++) {

        final Version version = new Version(major, minor);
        results.add(version.toString());

        version.setModifierSeparator(minVersion.getModifierSeparator());
        version.setModifier(minVersion.getModifier());
        results.add(version.toString());

        version.setModifierSeparator(maxVersion.getModifierSeparator());
        version.setModifier(maxVersion.getModifier());
        results.add(version.toString());

      }
    }

    return results;
  }

  /**
   * Supplies a pattern match which will detect major.minor, major.minor-modifier as well as major.minor, major.minor.patch-modifier.
   * 
   * @return a valid Pattern object.
   */
  @Override
  public Pattern getPatternMatcher() {
    return MAJOR_MINOR_PATTERN;
  }
}
