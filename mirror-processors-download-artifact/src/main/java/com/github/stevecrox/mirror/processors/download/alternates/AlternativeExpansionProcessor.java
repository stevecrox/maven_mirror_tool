package com.github.stevecrox.mirror.processors.download.alternates;

import java.util.HashSet;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import com.github.stevecrox.mirror.exceptions.download.AlternateGenerationException;
import com.github.stevecrox.mirror.interfaces.DownloadArtifact;
import com.github.stevecrox.mirror.interfaces.ModifierArtifact;
import com.github.stevecrox.mirror.interfaces.PackageArtifact;

/**
 * Generates a download artifact for the packaging type supplied, if the artifact supplied supports modifiers ad the packaging type contains them ths will also create a download
 * artifact for each modifier associated with the new packaging type.
 */
public final class AlternativeExpansionProcessor implements Processor {

  /**
   * 
   * @param exchange
   * @throws Exception
   */
  public void process(final Exchange exchange) throws Exception {
    final Message inMessage = exchange.getIn();
    final Message outMessage = exchange.getOut();

    //
    final Object body = inMessage.getBody();
    if (body instanceof DownloadArtifact) {
      final DownloadArtifact artifact = (DownloadArtifact) body;
      final Set<DownloadArtifact> results = new HashSet<DownloadArtifact>();

      // retrieve the packager, so we can find out what additional file types are associated.
      final PackageArtifact packaging = artifact.getPackaging();
      if (null != packaging) {

        final PackageArtifact[] alternates = packaging.getAlternatePackagingTypes();
        if (null != alternates) {
          // Iterate over all possible alternate types we have attached for the supplied
          for (final PackageArtifact alternate : alternates) {
            final Set<DownloadArtifact> artifacts = this.processPackageArtifact(artifact, alternate);
            results.addAll(artifacts);
          }
        }
      }

      // only supply an out message if we actually get results to process.
      if (results.isEmpty()) {
        throw new AlternateGenerationException("Artifact had no alternates:\t" + artifact.toString());
      }

      outMessage.setBody(results, HashSet.class);

    } else {
      throw new AlternateGenerationException("Artifact was wrong type unable to process");
    }
  }

  /**
   * Generates a download artifact for the packaging type supplied, if the artifact supplied supports modifiers ad the packaging type contains them ths will also create a download
   * artifact for each modifier associated with the new packaging type.
   * 
   * @param artifact
   * @param alternate
   * @return
   * @throws CloneNotSupportedException
   */
  private Set<DownloadArtifact> processPackageArtifact(final DownloadArtifact artifact, final PackageArtifact alternate) throws CloneNotSupportedException {
    final Set<DownloadArtifact> results = new HashSet<DownloadArtifact>();

    // If we have no modifiers just clone the object.
    if (null == alternate.getModifiers()) {
      final DownloadArtifact clone = (DownloadArtifact) artifact.clone();
      clone.setPackaging(alternate);
      results.add(clone);

    } else if (artifact instanceof ModifierArtifact && 0 < alternate.getModifiers().length) {
      final ModifierArtifact modArt = (ModifierArtifact) artifact;

      // Create a new Object for each modifier associated with the packaging type.
      for (final String modifier : alternate.getModifiers()) {
        // if we don't have an object which supports modifiers then just clone and change the packaging type.
        final DownloadArtifact clone = (DownloadArtifact) modArt.clone();
        clone.setPackaging(alternate);
        clone.setDestination(null);
        clone.setSource(null);
        // we already tested the objects type
        ((ModifierArtifact) clone).setModifier(modifier);
        results.add(clone);
      }
    } else {
      // if we don't have an object which supports modifiers then just clone and change the packaging type.
      final DownloadArtifact clone = (DownloadArtifact) artifact.clone();
      clone.setPackaging(alternate);
      results.add(clone);
    }

    return results;
  }

}
