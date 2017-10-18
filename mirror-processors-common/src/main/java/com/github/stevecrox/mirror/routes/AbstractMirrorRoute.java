package com.github.stevecrox.mirror.routes;

import org.apache.camel.builder.RouteBuilder;

import com.github.stevecrox.mirror.exceptions.routes.RouteSetupException;

public abstract class AbstractMirrorRoute extends RouteBuilder {
  /** The location we want to retrieve the input of the route from. */
  private final String entryPoint;
  /** The location we want to transmit the results of the route to. */
  private final String[] exitPoint;

  /**
   * Constructor, sets the entry and exist points for the route.
   * 
   * @param source
   *          The queue/camel entry point the input data can be ingested from.
   * @param destinaton
   *          The location the parsed CSV data will be sent for further processing
   * @throws RouteSetupException
   *           thrown if the entry/exit points were invalid.
   */
  public AbstractMirrorRoute(final String source, final String... destinaton) throws RouteSetupException {
    super();

    if (null == source || source.trim().isEmpty()) {
      throw new RouteSetupException("Invalid source was supplied");
    }
    this.entryPoint = source;

    if (null == destinaton || 0 == destinaton.length) {
      throw new RouteSetupException("Invalid destination was supplied.");
    }

    this.exitPoint = destinaton;
  }

  /**
   * Retrieves Camel Entrypoint string which represents the entry point e.g. queue, direct, etc...
   * 
   * @return a valid camel entry point.
   */
  public String getEntryPoint() {
    return this.entryPoint;
  }

  /**
   * The exit location, e.g. a queue, direct, etc..
   * 
   * @return a valid camel end point.
   */
  public String[] getExitPoint() {
    return exitPoint;
  }

}
