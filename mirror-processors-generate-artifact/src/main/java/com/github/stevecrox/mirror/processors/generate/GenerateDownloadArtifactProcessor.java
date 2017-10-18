package com.github.stevecrox.mirror.processors.generate;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import com.github.stevecrox.mirror.exceptions.generate.GenerateArtifactRouteException;
import com.github.stevecrox.mirror.interfaces.GenerateArtifact;

/**
 * This class will take in an GenerateArtifact and ask it to generate all of the required DownloadArtifacts and return those.
 */
public final class GenerateDownloadArtifactProcessor implements Processor {

  /**
   * This function will take in an GenerateArtifact and ask it to generate all of the required DownloadArtifacts and return those.
   * 
   * @param exchange body contains a GenerateArtifact
   * @throws GenerateArtifactRouteException if the exchange object isn't a GenerateArtifact.
   * @throws ArtifactGenerationException if there is an issue generating versions.
   */
  public void process(final Exchange exchange) throws Exception {
    final Message inMessage = exchange.getIn();
    final Message outMessage = exchange.getOut();
    // retrieves the message body which we expect to define a maven group id and then artifact
    // identifier.
    final Object body = inMessage.getBody();
    if (body instanceof GenerateArtifact) {
      final GenerateArtifact artifact = (GenerateArtifact) body;

      // generate all of the download artifacts.
      outMessage.setBody(artifact.generateArtifacts());
    } else {
      throw new GenerateArtifactRouteException("Invalid message object was supplied");
    }
  }
}
