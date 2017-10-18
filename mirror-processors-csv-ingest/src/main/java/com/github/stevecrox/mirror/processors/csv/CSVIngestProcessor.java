package com.github.stevecrox.mirror.processors.csv;

import java.util.Collection;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import com.github.stevecrox.mirror.exceptons.csv.CSVParserException;
import com.github.stevecrox.mirror.interfaces.GenerateArtifact;
import com.github.stevecrox.mirror.interfaces.PackageArtifact;

public class CSVIngestProcessor implements Processor {

    /**
     * This will load the Text string from the system and convert it into a GenerateArtifact.
     * 
     * @param exchange
     *            the message, expected to contain a CSV string which represents the request
     * @throws Exception
     *             only throws a CSVParserException, this is thrown if there is an issue with the CSV string supplied.
     */
    public void process(final Exchange exchange) throws Exception {
        final Message inMessage = exchange.getIn();
        final Message outMessage = exchange.getOut();
        // retrieves the message body which we expect to define a maven group id and then artifact
        // identifier.
        final Object body = inMessage.getBody();
        if (body instanceof Collection<?>) {
            final Collection<?> bodyData = (Collection<?>) body;
            final Object[] cells = bodyData.toArray();

            if (null == cells || cells.length == 0) {
                throw new CSVParserException("String did not contain array are expected:\t" + cells.toString());
            }

            final GenerateArtifact artifact = this.findGenerateArtifact(cells);
            if (null == artifact || null == artifact.getPackaging()) {
                throw new CSVParserException("No artifact found for CSV row:\t" + cells.toString());
            }

            final int cellPos = this.getPackagingPosition(artifact.getPackaging(), cells);
            final int artifactPos = artifact.getPackagingPosition();

            // assume all fields correlate
            if (cellPos == artifactPos) {
                for (int index = 0; index < cells.length; index++) {
                    artifact.setField(index, cells[index].toString());
                }
            } else if (cellPos < artifactPos) {
                // set the initial fields
                for (int index = 0; index < cellPos; index++) {
                    artifact.setField(index, cells[index].toString());
                }

                final int diff = artifactPos - cellPos;
                for (int index = artifactPos; index < (cells.length + diff); index++) {
                    artifact.setField(index, cells[index - diff].toString());
                }

            } else {
                throw new CSVParserException("Two many cells in supplied data:\t" + cells.toString());
            }

            outMessage.setBody(artifact);

        } else {
            throw new CSVParserException("Invalid object type was supplied, unable to parse.");
        }
    }

    private int getPackagingPosition(final PackageArtifact packaging, final Object[] cells) {

        int result = -1;

        for (int index = 0; index < cells.length; index++) {
            if (packaging.getPackagingType().equalsIgnoreCase(cells[index].toString())) {
                result = index;
                break;
            }
        }

        return result;
    }

    /**
     * This uses the service loader to load all possible generate artifact objects and looks to see if they match
     * 
     * @param cells
     *            the csv columns to be parsed into a generate artifact, assumption is one will contain the packging type of the desired object.
     * @return null if no generate artifact can be found
     * @throws CSVParserException
     *             thrown if a generate artifact has no packaging type.
     */
    private GenerateArtifact findGenerateArtifact(final Object[] cells) throws CSVParserException {
        final ServiceLoader<GenerateArtifact> loader = ServiceLoader.load(GenerateArtifact.class);
        final Iterator<GenerateArtifact> iterator = loader.iterator();

        GenerateArtifact result = null;

        while (iterator.hasNext()) {
            final GenerateArtifact artifact = iterator.next();
            final PackageArtifact packaging = artifact.getPackaging();

            boolean foundArtifact = false;
            if (null == packaging || null == packaging.getPackagingType()) {
                throw new CSVParserException("Generate Artifact had now packaging type associated with it");
            } else {
                // start from the final column as this is most likely the type column
                for (final Object cell : cells) {
                    if (packaging.getPackagingType().equalsIgnoreCase(cell.toString())) {
                        foundArtifact = true;
                        break;
                    }
                }
            }

            if (foundArtifact) {
                result = artifact;
                break;
            }

        }

        return result;
    }
}
