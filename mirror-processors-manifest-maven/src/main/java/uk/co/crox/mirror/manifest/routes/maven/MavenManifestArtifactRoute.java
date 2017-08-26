package uk.co.crox.mirror.manifest.routes.maven;

import java.net.URI;

import uk.co.crox.mirror.exceptions.routes.RouteSetupException;
import uk.co.crox.mirror.manifest.exceptions.maven.MavenManifestRouteException;
import uk.co.crox.mirror.manifest.exceptions.maven.MissingManifestException;
import uk.co.crox.mirror.manifest.processors.maven.LoadParentPOMsProcessor;
import uk.co.crox.mirror.manifest.processors.maven.MavenManifestValidatorProcessor;
import uk.co.crox.mirror.manifest.processors.maven.dependencies.LoadDependenciesProcessor;
import uk.co.crox.mirror.manifest.processors.maven.dependencies.LoadDependencyManagementProcessor;
import uk.co.crox.mirror.manifest.processors.maven.dependencies.MergeDependencyManagementIntoDependencies;
import uk.co.crox.mirror.manifest.processors.maven.dependencies.MergePropertiesIntoDependencyManagementProcessor;
import uk.co.crox.mirror.manifest.processors.maven.dependencies.MergePropertiesIntoDependencyProcessor;
import uk.co.crox.mirror.manifest.processors.maven.exception.ExtractMissingManifestFromExceptionProcessor;
import uk.co.crox.mirror.manifest.processors.maven.properties.LoadPOMPropertiesProcessor;
import uk.co.crox.mirror.manifest.processors.maven.properties.Maven3PropertiesProcessor;
import uk.co.crox.mirror.manifest.processors.maven.properties.MavenProjectPropertiesProcessor;
import uk.co.crox.mirror.routes.AbstractMirrorRoute;

/**
 * This route is designed to first generate the remote url the artifact will be retrieved from, then it will test to see if something exists, then it will look to see if this
 * artifact has already been retrieved and then lastly it will download the file.
 */
public final class MavenManifestArtifactRoute extends AbstractMirrorRoute {
  /** Handle Parent POM queue. */
  private static final String PARENT_POM_QUEUE = "activemq:queue:handleParentPOM";
  /** Handle Parent POM queue. */
  private static final String REPROCESS_POM_QUEUE = "activemq:queue:ReprocessPOM";
  /** The target repository to check the dependencies out into. */
  private final URI destination;
  /** The location we want to retrieve the input of the route from. */
  private final String downloadQueue;

  /**
   * Constructor, sets the entry and exist points for the ingest route.
   * 
   * @param source
   *          The queue/camel entry point the input data can be ingested from.
   * @param destinaton
   *          The location the parsed CSV data will be sent for further processing
   * @throws RouteSetupException
   *           thrown if the entry/exit points were invalid.
   * @throws MavenManifestRouteException
   *           thrown if the repository point was invalid.
   */
  public MavenManifestArtifactRoute(final String source, final String download, final URI repository, final String... destinatons) throws RouteSetupException, MavenManifestRouteException {
    super(source, destinatons);

    if (null == repository) {
      throw new MavenManifestRouteException("Supplied URI for target URL was invalid.");
    }
    this.destination = repository;
    
    if (null == download || download.trim().isEmpty()) {
      throw new MavenManifestRouteException("Invalid source was supplied");
    }
    this.downloadQueue = download;
  }

  /**
   * This route is designed to first generate the remote url the artifact will be retrieved from, then it will test to see if something exists, then it will look to see if this
   * artifact has already been retrieved and then lastly it will download the file.
   */
  @Override
  public void configure() throws Exception {

    onException(MavenManifestRouteException.class).handled(true).to("log:nofile");
    // Handle the loading of various POM's
    onException(MissingManifestException.class)
      .handled(true)
      .multicast().parallelProcessing()
      .to(PARENT_POM_QUEUE, REPROCESS_POM_QUEUE);

    //
    from(PARENT_POM_QUEUE)
      .process(new ExtractMissingManifestFromExceptionProcessor(this.getDestination()))
      .to(this.getDownloadQueue());
    
    //
    from(REPROCESS_POM_QUEUE)
      .delay(10000)
      .to(this.getEntryPoint());

    //
    from(this.getEntryPoint())
      .process(new MavenManifestValidatorProcessor())
      .process(new LoadParentPOMsProcessor(this.getDestination()))
      .process(new Maven3PropertiesProcessor())
      .process(new MavenProjectPropertiesProcessor())
      .process(new LoadPOMPropertiesProcessor(this.getDestination()))
      // setup the dependency management section
      .process(new LoadDependencyManagementProcessor(this.getDestination()))
      .process(new MergePropertiesIntoDependencyManagementProcessor())
      // load all dependencies
      .process(new LoadDependenciesProcessor(this.getDestination()))
      .process(new MergePropertiesIntoDependencyProcessor())
      .process(new MergeDependencyManagementIntoDependencies())
      .to(this.getExitPoint());
  }

  /**
   * Retrieves the target repository to check the dependencies out into.
   * 
   * @return a valid URI location.
   */
  public URI getDestination() {
    return destination;
  }

  /**
   * @return the downloadQueue
   */
  public String getDownloadQueue() {
    return downloadQueue;
  }
}
