package uk.co.crox.mirror.processors.generate;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import uk.co.crox.mirror.dto.Version;
import uk.co.crox.mirror.exceptions.generate.GenerateArtifactRouteException;
import uk.co.crox.mirror.exceptions.generate.VerisonExpansionException;
import uk.co.crox.mirror.interfaces.GenerateArtifact;

/**
 * This class is designed to ingest GenerateArtifact objects, if that generate artifact object has a version range specified this will generate DownloadArtifacts for each version
 * inside of the range. If no range is specified this will generate a single DownloadArtifact with the basic version included.
 */
public abstract class AbstractVersionExpansionProcessor implements Processor {

  /** Max version to end on when minor/patch is less then major/minor. */
  protected static final int MAX_VERSION = 10;
  /** Min version to start when minor/patch is less then major/minor. */
  protected static final int MIN_VERSION = 0;

  /** Number of matches for a Semantic version design. */
  private static final int MAJOR_MINOR_GROUP_NUM = 2;
  /** Number of matches for a Semantic version design. */
  private static final int SEMANTIC_GROUP_NUM = 3;
  /** Number of matches for a Semantic version design. */
  private static final int MAJOR_MINOR_MODIFIER_GROUP_NUM = 4;
  /** Number of matches for a Semantic version design. */
  private static final int SEMANTIC_MODIFIER_GROUP_NUM = 5;

  /**
   * This method is supplied a matcher based on the pattern matcher, the expectation is the pattern matcher either caught a major.minor or a major.minor.patch version string. This
   * will then generate a version object to represent this, this will also capture version modifiers e.g. 4.1.2-RELEASE and add them into the returned object.
   * 
   * @param matched
   *          Matcher generate from the pattern applied to version string, expectation is the groups will be 2-5.
   * @return null if the expected number of matches didn't occur.
   */
  public Version createVersion(final Matcher matched) {
    final int groupCount = matched.groupCount();

    final Version result;
    if (groupCount == SEMANTIC_GROUP_NUM || groupCount == SEMANTIC_MODIFIER_GROUP_NUM) {
      final int major = Integer.parseInt(matched.group(1));
      final int minor = Integer.parseInt(matched.group(2));
      final int patch = Integer.parseInt(matched.group(3));

      if (groupCount == SEMANTIC_MODIFIER_GROUP_NUM) {
        result = new Version(major, minor, patch, matched.group(4), matched.group(5));
      } else {
        result = new Version(major, minor, patch);
      }
    } else if (groupCount == MAJOR_MINOR_GROUP_NUM || groupCount == MAJOR_MINOR_MODIFIER_GROUP_NUM) {
      final int major = Integer.parseInt(matched.group(1));
      final int minor = Integer.parseInt(matched.group(2));

      if (groupCount == SEMANTIC_MODIFIER_GROUP_NUM) {
        result = new Version(major, minor, matched.group(3), matched.group(4));
      } else {
        result = new Version(major, minor);
      }
    } else {
      result = null;
    }

    return result;
  }

  /**
   * This will generate a list of versions, from the minimum version supplied up to the maximum possible version, these will then be returned. When generating the versions this
   * will create three versions, the version, the version with the min version modifier and the version with the max version modifier. A Set is used to ensure all strings held are
   * unique.
   * 
   * The method expects the min version to be less than or equal to than the max and doesn't handle the inverse position.
   * 
   * @param minVersion
   *          the minimum possible version e.g. 4.1.2_Beta
   * @param maxVersion
   *          the maximum possible version e.g 4.1.5-Release
   * @return an empty list if there aren't any versions.
   */
  public abstract Set<String> generateVersions(final Version minVersion, final Version maxVersion);

  /**
   * This is called by the generate versions function, the expectation is two versions are passed in e.g. min: 4.1.2, max: 4.2.5. If 4.2.2 is the minimum version we want to import
   * then the version range is 4.1.2-4.1.10 and 4.2.0-4.2.5.
   * 
   * This should be supplied the current level (e.g. Major version: 4) along with the maximum level for the current level (e.g. major version: 4), if the current is less than the
   * maximum then we want a full spread of values in the next level (minor : 10), if the currentl level is at the maximum then we want to constrain the next level don the the
   * maximum allowed (e.g. 2).
   * 
   * @param currentLevelValue
   *          the current level we are processing
   * @param curentLevelMax
   *          the maximum value the current level can have
   * @param nextLevelMax
   *          the maximum value the next level down can have
   * @return MAX_VERSION if the current level is less than it's maximum, otherwise the nextLevelMax.
   */
  protected int getEndPosition(final int currentLevelValue, final int curentLevelMax, final int nextLevelMax) {
    final int result;

    if (currentLevelValue < curentLevelMax) {
      result = MAX_VERSION;
    } else {
      result = nextLevelMax;
    }

    return result;
  }

  /**
   * Pattern object containing the regular expression we need to test the version strings against.
   * 
   * @return a valid pattern object.
   */
  public abstract Pattern getPatternMatcher();

  /**
   * This is called by the generate versions function, the expectation is two versions are passed in e.g. min: 4.1.2, max: 4.2.5. If 4.2.2 is the minimum version we want to import
   * then the version range is 4.1.2-4.1.10 and 4.2.0-4.2.5.
   * 
   * This should be supplied the current level (e.g. Major version: 4) along with the minimum level for the current level (e.g. major version: 4), if the current level value is
   * greater to the minimum then we want to start at zero. If the currentl level is at the minimum then we want to constrain the next level to the next levels starting value (e.g.
   * 1).
   * 
   * @param currentLevelValue
   *          the current level we are processing
   * @param curentLevelMin
   *          the minimum value the current level can have
   * @param nextLevelMin
   *          the minimum value the next level down can have
   * @return MIN_VERSION if the current level is greater than it's minimum, otherwise the nextLevelMin.
   */
  protected int getStartPosition(final int currentLevelValue, final int curentLevelMin, final int nextLevelMin) {
    final int result;

    if (currentLevelValue > curentLevelMin) {
      result = MIN_VERSION;
    } else {
      result = nextLevelMin;
    }

    return result;
  }

  /**
   * This class is designed to ingest GenerateArtifact objects, if that generate artifact object has a version range specified this will generate DownloadArtifacts for each version
   * inside of the range. If no range is specified this will generate a single DownloadArtifact with the basic version included.
   * 
   * @param exchange
   *          a message containing an artifact to generate
   */
  public void process(final Exchange exchange) throws Exception {
    final Message inMessage = exchange.getIn();

    // retrieves the message body which we expect to define a maven group id and then artifact
    // identifier.
    final Object body = inMessage.getBody();
    if (body instanceof GenerateArtifact) {
      final GenerateArtifact artifact = (GenerateArtifact) body;

      if (null == artifact.getMinVersion()) {
        throw new VerisonExpansionException("No Version data supplied for artifact:\t" + artifact.toString());
      }

      final Set<String> versions;
      if (null == artifact.getVersions()) {
        versions = new HashSet<String>();
        artifact.setVersion(versions);
      } else {
        versions = artifact.getVersions();
      }

      // If there is no artifact range, then use the minimum version value
      if (null == artifact.getMaxVersion() || artifact.getMaxVersion().trim().isEmpty() || artifact.getMinVersion().equals(artifact.getMaxVersion())) {
        versions.add(artifact.getMinVersion());
      } else {
        final Matcher minMatcher = this.getPatternMatcher().matcher(artifact.getMinVersion());
        final Matcher maxMatcher = this.getPatternMatcher().matcher(artifact.getMaxVersion());

        // Take the version strings and see if they match a type which is supported by expansion
        if (minMatcher.matches() && maxMatcher.matches()) {
          final Version minVersion = this.createVersion(minMatcher);
          final Version maxVersion = this.createVersion(maxMatcher);

          // work out which version object is smaller and then generate the versions between the two values.
          final int comparison = minVersion.compare(maxVersion);
          if (comparison > 0) {
            versions.addAll(this.generateVersions(maxVersion, minVersion));
          } else {
            versions.addAll(this.generateVersions(minVersion, maxVersion));
          }
        }
      }
    } else {
      throw new GenerateArtifactRouteException("No Artifact to generate was supplied");
    }
  }
}
