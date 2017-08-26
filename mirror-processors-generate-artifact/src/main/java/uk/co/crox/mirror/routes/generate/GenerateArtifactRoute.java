package uk.co.crox.mirror.routes.generate;

import java.util.HashSet;

import uk.co.crox.mirror.exceptions.ArtifactGenerationException;
import uk.co.crox.mirror.exceptions.generate.GenerateArtifactRouteException;
import uk.co.crox.mirror.exceptions.generate.VerisonExpansionException;
import uk.co.crox.mirror.exceptions.routes.RouteSetupException;
import uk.co.crox.mirror.processors.generate.GenerateDownloadArtifactProcessor;
import uk.co.crox.mirror.processors.generate.MajorMinorVersionExpansionProcessor;
import uk.co.crox.mirror.processors.generate.SemanticVersionExpansionProcessor;
import uk.co.crox.mirror.routes.AbstractMirrorRoute;

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
