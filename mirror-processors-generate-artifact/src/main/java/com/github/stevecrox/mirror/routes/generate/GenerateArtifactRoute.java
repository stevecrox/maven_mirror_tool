package com.github.stevecrox.mirror.routes.generate;

import java.util.HashSet;

import com.github.stevecrox.mirror.exceptions.ArtifactGenerationException;
import com.github.stevecrox.mirror.exceptions.generate.GenerateArtifactRouteException;
import com.github.stevecrox.mirror.exceptions.generate.VerisonExpansionException;
import com.github.stevecrox.mirror.exceptions.routes.RouteSetupException;
import com.github.stevecrox.mirror.processors.generate.GenerateDownloadArtifactProcessor;
import com.github.stevecrox.mirror.processors.generate.MajorMinorVersionExpansionProcessor;
import com.github.stevecrox.mirror.processors.generate.SemanticVersionExpansionProcessor;
import com.github.stevecrox.mirror.routes.AbstractMirrorRoute;

/**
 * This route is designed to ingest GenerateArtifacts, process them, expand the possible range and then generate DownloadArtifacts which are passed on.
 */
public final class GenerateArtifactRoute extends AbstractMirrorRoute {
  /**
   * Constructor, sets the entry and exist points for the ingest route.
   * 
   * @param source
   *          The queue/camel entry point the input data can be ingested from.
   * @param destinaton
   *          The location the parsed CSV data will be sent for further processing
   * @throws RouteSetupException
   *           thrown if the entry/exit points were invalid.
   */
  public GenerateArtifactRoute(final String source, final String destinaton) throws RouteSetupException {
    super(source, destinaton);
  }

  @Override
  public void configure() throws Exception {

    onException(VerisonExpansionException.class).handled(true).to("log:nofile");
    onException(GenerateArtifactRouteException.class).handled(true).to("log:nofile");
    onException(ArtifactGenerationException.class).handled(true).to("log:nofile");

    from(this.getEntryPoint())
      .process(new SemanticVersionExpansionProcessor())
      .process(new MajorMinorVersionExpansionProcessor())
      .process(new GenerateDownloadArtifactProcessor())
      .split().body(HashSet.class)
      .to(this.getExitPoint());
  }
}
