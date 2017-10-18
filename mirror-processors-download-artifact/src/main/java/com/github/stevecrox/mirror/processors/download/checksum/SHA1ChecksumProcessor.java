package com.github.stevecrox.mirror.processors.download.checksum;

import java.net.URI;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import com.github.stevecrox.mirror.interfaces.DownloadArtifact;

public class SHA1ChecksumProcessor implements Processor {

  /** Checksum file extension. */
  private static final String CHECKSUM = ".sha1";
  
  /**
   * 
   * @param exchange
   * @throws Exception
   */
  public void process(final Exchange exchange) throws Exception {
    final Message inMessage = exchange.getIn();

    //
    final Object body = inMessage.getBody();
    if (body instanceof DownloadArtifact) {
      final DownloadArtifact artifact = (DownloadArtifact) body;
      
      final URI source = artifact.getSource();
      final String sourceChecksum = source.toString() + CHECKSUM;
      artifact.setSource(URI.create(sourceChecksum));
      
      final URI destination = artifact.getDestination();
      final String destinationChecksum = destination.toString() + CHECKSUM;
      artifact.setDestination(URI.create(destinationChecksum));
    }
  }

}
