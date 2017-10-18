package com.github.stevecrox.mirror.routes.csv;

import java.io.File;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.csv.CsvDataFormat;

import com.github.stevecrox.mirror.exceptons.csv.CSVParserException;
import com.github.stevecrox.mirror.processors.csv.CSVIngestProcessor;

public final class CSVIngestRoute extends RouteBuilder {
  /** The directory/file we want to ingest. */
  private final String entryPoint;
  /** The location we want to transmit the results of the route to. */
  private final String exitPoint;

  /**
   * Constructor, sets the entry and exist points for the ingest route.
   * 
   * @param source
   *          The directory of CSV files and/or the CSV file we are looking to process.
   * @param destination
   *          The location the parsed CSV data will be sent for further processing
   * @throws CSVParserException
   *           thrown if the supplied file object was invalid.
   */
  public CSVIngestRoute(final File source, final String destinaton) throws CSVParserException {
    super();

    if (null == source) {
      throw new CSVParserException("Invalid File Object was supplied");
    }

    if (source.isDirectory()) {
      entryPoint = String.format("file:%s%s", source, File.separator);
    } else if (source.isFile()) {
      entryPoint = String.format("file:%s%s?fileName=%s", source, File.separator, source.getName());
    } else {
      throw new CSVParserException("File object wasn'ta file or directory");
    }

    if (null == destinaton || destinaton.trim().isEmpty()) {
      throw new CSVParserException("Invalid destination was supplied.");
    }

    this.exitPoint = destinaton;

  }

  @Override
  public void configure() throws Exception {

    onException(CSVParserException.class).handled(true).to("log:nofile");

    final CsvDataFormat csv = new CsvDataFormat();
    csv.setDelimiter(',');

    from(this.getEntryPoint()).unmarshal(csv).split().body()
        // parses each CSV line into an artifact
        .process(new CSVIngestProcessor()).to(this.getExitPoint());
  }

  /**
   * Retrieves Camel Entrypoint string which represents the directory of CSV files and/or the CSV file we are looking to process.
   * 
   * @return valid object, which constracts a valid route.
   */
  public String getEntryPoint() {
    return this.entryPoint;
  }

  /**
   * The exist location, e.g. a queue, direct, etc..
   * 
   * @return a valid camel end point.
   */
  public String getExitPoint() {
    return exitPoint;
  }
}
