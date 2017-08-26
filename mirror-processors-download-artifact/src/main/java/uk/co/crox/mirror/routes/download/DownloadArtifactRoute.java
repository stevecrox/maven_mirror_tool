package uk.co.crox.mirror.routes.download;

import java.net.URI;

import uk.co.crox.mirror.exceptions.DownloadArtifactException;
import uk.co.crox.mirror.exceptions.download.AlternateGenerationException;
import uk.co.crox.mirror.exceptions.download.DestinationExistsException;
import uk.co.crox.mirror.exceptions.download.DownloadArtifactRouteException;
import uk.co.crox.mirror.exceptions.download.NotManifestException;
import uk.co.crox.mirror.exceptions.routes.RouteSetupException;
import uk.co.crox.mirror.exceptions.url.GenerateDestinationURIException;
import uk.co.crox.mirror.exceptions.url.URLCheckerException;
import uk.co.crox.mirror.processors.download.FileDownloadProcessor;
import uk.co.crox.mirror.processors.download.alternates.AlternativeExpansionProcessor;
import uk.co.crox.mirror.processors.download.checksum.MD5ChecksumProcessor;
import uk.co.crox.mirror.processors.download.checksum.SHA1ChecksumProcessor;
import uk.co.crox.mirror.processors.download.manifests.ManifestValidatorProcessor;
import uk.co.crox.mirror.routes.AbstractMirrorRoute;
import uk.co.crox.mirror.uri.processors.GenerateDestinationURIProcessor;
import uk.co.crox.mirror.uri.processors.GenerateSourceURIProcessor;

/**
 * This route is designed to first generate the remote url the artifact will be retrieved from, then it will test to see if something exists, then it will look to see if this
 * artifact has already been retrieved and then lastly it will download the file.
 */
public final class DownloadArtifactRoute extends AbstractMirrorRoute {
 
  /** MD5 Checksum queue. */
  private static final String MD5_QUEUE = "activemq:queue:md5Checksumqueue";
  /** SHA-1 Checksum queue. */
  private static final String SHA1_QUEUE = "activemq:queue:sha1Checksumqueue";
  /** Generate Alternate packaging types queue. */
  private static final String ALTERNATES_QUEUE = "activemq:queue:expandOutAlternatives";
  /** Validate for manifests before passing to exit queue. */
  private static final String MANIFEST_QUEUE = "activemq:queue:validateIsManifest";
  
  
  /** The target repository to check the dependencies out into. */
  private final URI destination;
  
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
  public DownloadArtifactRoute(final String source, final URI repository, final String... destinatons) throws RouteSetupException {
    super(source, destinatons);
    
    if (null == repository) {
      throw new RouteSetupException("Supplied URI for target URL was invalid.");
    }
    this.destination = repository;
  }

  /**
   * This route is designed to first generate the remote url the artifact will be retrieved from, then it will test to see if something exists, then it will look to see if this
   * artifact has already been retrieved and then lastly it will download the file.
   */
  @Override
  public void configure() throws Exception {

    onException(DownloadArtifactRouteException.class).handled(true).to("log:nofile");
    onException(AlternateGenerationException.class).handled(true).to("log:nofile");
    onException(DownloadArtifactException.class).handled(true).to("log:nofile");
    onException(GenerateDestinationURIException.class).handled(true).to("log:nofile");
    onException(URLCheckerException.class).handled(true).to("log:nofile");
    onException(NotManifestException.class).handled(true).to("log:nofile");
    
    // If the source exists then check it isn't a manifest which needs downloading.
    onException(DestinationExistsException.class).handled(true).to(MANIFEST_QUEUE);
        
    from(this.getEntryPoint())
      .process(new GenerateDestinationURIProcessor(this.getDestination()))
      .process(new GenerateSourceURIProcessor())
      .process(new FileDownloadProcessor())
      .multicast().parallelProcessing()
        .to(MD5_QUEUE, SHA1_QUEUE, ALTERNATES_QUEUE, MANIFEST_QUEUE);
    
    from(MD5_QUEUE)
      .process(new MD5ChecksumProcessor())
      .process(new FileDownloadProcessor());

    from(SHA1_QUEUE)
      .process(new SHA1ChecksumProcessor())
      .process(new FileDownloadProcessor());
    
    // Expands out the packaging type (and modifiers) so all expected associated files are retrieved
    from(ALTERNATES_QUEUE)
      .process(new AlternativeExpansionProcessor())
      .split(body())
      .to(this.getEntryPoint());
    
    // ensures only manifests are passed on for processing.
    from(MANIFEST_QUEUE)
      .process(new ManifestValidatorProcessor())
      .to(this.getExitPoint());
  }
  
  /**
   * Retrieves the target repository to check the dependencies out into.
   * @return a valid URI location.
   */
  public URI getDestination() {
    return destination;
  }
}
