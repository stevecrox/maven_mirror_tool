package meh;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.stevecrox.mirror.dto.Version;

public class Test {
  /** Semantic version patterned pattern matcher. */
  private static final String FORMAT = "(\\d+)\\.(\\d+)(?:\\.)?(\\d*)(\\.|-|\\+)?([0-9A-Za-z-.]*)?";
  /** Number of matches for a Major/Minor design. */
  private static final int MAJOR_MINOR_GROUP_NUM = 2;
  
  /** Semantic version patterned pattern matcher. */
  private static final String SEMANTIC_FORMAT = "(\\d+)\\.(\\d+)\\.(\\d+)(\\.|[-_]|\\+)?([0-9A-Za-z-.]*)?";
  /** Number of matches for a Semantic version design. */
  private static final int SEMANTIC_GROUP_NUM = 3;
  /** Number of matches for a Semantic version design. */
  private static final int MODIFIER_GROUP_NUM = 5;
  
  /** Min version to start when minor/patch is less then major/minor. */
  private static final int MIN_VERSION = 0;
  /** Max version to end on when minor/patch is less then major/minor. */
  private static final int MAX_VERSION = 10;

  
  @org.junit.Test
  public void test() {
   final String minVersionVal = "4.1.2-RELEASE";
   final String maxVersionVal = "5.1.5_RELEASE";
   
   final Pattern pattern = Pattern.compile(SEMANTIC_FORMAT);
   final Matcher minMatcher = pattern.matcher(minVersionVal);
   final Matcher maxMatcher = pattern.matcher(maxVersionVal);
   
   if (minMatcher.matches() && maxMatcher.matches()) {
     final Version minVersion = this.createVersion(minMatcher);
     final Version maxVersion = this.createVersion(maxMatcher);
     
     final int comparison = minVersion.compare(maxVersion);
     if (comparison > 0) {
       this.generateVersions(maxVersion, minVersion);
     } else if (comparison < 0) {
       this.generateVersions(minVersion, maxVersion);
     } else {
       
     }
   }
  }
  
  private Version createVersion(final Matcher matched) {
    final int groupCount = matched.groupCount();
    
    final Version result;
    if (groupCount >= SEMANTIC_GROUP_NUM) {
      final int major = Integer.parseInt(matched.group(1));
      final int minor = Integer.parseInt(matched.group(2));
      final int patch = Integer.parseInt(matched.group(3));
      
      if (groupCount >= MODIFIER_GROUP_NUM) {
        result = new Version(major, minor, patch, matched.group(4), matched.group(5));
      } else {
        result = new Version(major, minor, patch);
      }
    } else {
      result = null;
    }
    
    return result;
  }
  
  private Set < String > generateVersions(final Version minVersion, final Version maxVersion) {
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
  
  private int getStartPosition(final int current, final int startCurrent, final int startValue) {
    final int result;
    
    if (current != startCurrent) {
      result = MIN_VERSION;
    } else {
      result = startValue;
    }
    
    return result;
  }
  
  private int getEndPosition(final int current, final int endCurrent, final int endValue) {
    final int result;
    
    if (current < endCurrent) {
      result = MAX_VERSION;
    } else {
      result = endValue;
    }
    
    return result;
  }
}
