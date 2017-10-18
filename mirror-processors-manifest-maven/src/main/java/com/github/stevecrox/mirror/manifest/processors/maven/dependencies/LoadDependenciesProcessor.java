package com.github.stevecrox.mirror.manifest.processors.maven.dependencies;

import java.net.URI;
import java.util.Map;

import org.apache.maven.model.Model;

import com.github.stevecrox.mirror.dto.maven.MavenDependency;
import com.github.stevecrox.mirror.manifest.exceptions.maven.MavenManifestRouteException;

/**
 * 
 */
public final class LoadDependenciesProcessor extends AbstractLoadDependencyProcessor {

  /**
   * Constructor.
   * 
   * @param target
   * @throws DownloadArtifactRouteException
   */
  public LoadDependenciesProcessor(final URI target) throws MavenManifestRouteException {
    super(target);
  }

  @Override
  protected void processModel(final Map<String, Map<String, MavenDependency>> dependencies, final Model model) {    
    this.loadDependencies(dependencies, model.getDependencies());
  }
}
