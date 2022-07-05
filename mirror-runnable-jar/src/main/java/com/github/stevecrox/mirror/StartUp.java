package com.github.stevecrox.mirror;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.component.jms.JmsComponent;

import com.github.stevecrox.mirror.dto.maven.MavenDependency;
import com.github.stevecrox.mirror.exceptons.csv.CSVParserException;
import com.github.stevecrox.mirror.interfaces.DownloadArtifact;
import com.github.stevecrox.mirror.interfaces.GenerateArtifact;
import com.github.stevecrox.mirror.interfaces.PackageArtifact;
import com.github.stevecrox.mirror.manifest.routes.maven.MavenManifestArtifactRoute;
import com.github.stevecrox.mirror.routes.csv.CSVIngestRoute;
import com.github.stevecrox.mirror.routes.download.DownloadArtifactRoute;
import com.github.stevecrox.mirror.routes.generate.GenerateArtifactRoute;
import org.apache.camel.main.Main;

public class StartUp {

    private static final String GENERATE_ARTIFACT_QUEUE = "activemq:queue:generateDownloadArtifact";

    private static final String DOWNLOAD_ARTIFACT_QUEUE = "activemq:queue:downloadArtifact";

    private static final String PROCESS_MANIFEST_QUEUE = "activemq:queue:processManifest";

    private static final String LICENSE_QUEUE = "activemq:queue:license";

    /**
     * The expects two parameters, the first
     * @param args
     * @throws CSVParserException
     * @throws Exception
     */
    public static void main(final String[] args) throws CSVParserException, Exception {
        if (args.length == 2) {
            final File input = new File(args[0]);
            final File output = new File(args[1]);

            startCamel(input, output);
        }
    }

    /**
     * This will start up the application and parse the supplied CSV file and then start downloading the results
     * @param input
     * @param output
     * @throws CSVParserException
     * @throws Exception
     */
    private static void startCamel(final File input, final File output) throws CSVParserException, Exception {

        // create a Main instance
        final Main main = new Main();
        final CamelContext context = main.getCamelContext();

        final Set<String> trustedPackages = getTrusted(GenerateArtifact.class);
        trustedPackages.addAll(getTrusted(PackageArtifact.class));
        trustedPackages.addAll(getTrusted(DownloadArtifact.class));
        trustedPackages.addAll(getTrusted(GenerateArtifact.class));
        trustedPackages.addAll(getTrusted(PackageArtifact.class));
        // trustedPackages.add(MavenGenerateArtifact.class.getPackage().getName());
        trustedPackages.add(HashSet.class.getPackage().getName());
        trustedPackages.add(URI.class.getPackage().getName());
        trustedPackages.add(MavenDependency.class.getPackage().getName());

        final String brokerURL = "vm://localhost?broker.persistent=false";
        final ActiveMQConnectionFactory activeMQFactory = new ActiveMQConnectionFactory(brokerURL);
        activeMQFactory.setTrustedPackages(new ArrayList<String>(trustedPackages));

        final JmsComponent activemq = ActiveMQComponent.jmsComponent(activeMQFactory);
        activemq.setDeliveryPersistent(false);
        activemq.setMaxConcurrentConsumers(4);
        context.addComponent("activemq", activemq);

        final JmsComponent jms = JmsComponent.jmsComponent(activeMQFactory);
        jms.setDeliveryPersistent(false);
        jms.setMaxConcurrentConsumers(4);
        context.addComponent("jms", jms);

        context.addRoutes(new CSVIngestRoute(input, GENERATE_ARTIFACT_QUEUE));
        context.addRoutes(new GenerateArtifactRoute(GENERATE_ARTIFACT_QUEUE, DOWNLOAD_ARTIFACT_QUEUE));
        context.addRoutes(new DownloadArtifactRoute(DOWNLOAD_ARTIFACT_QUEUE, output.toURI(), PROCESS_MANIFEST_QUEUE));
        context.addRoutes(new MavenManifestArtifactRoute(PROCESS_MANIFEST_QUEUE, DOWNLOAD_ARTIFACT_QUEUE, output.toURI(), LICENSE_QUEUE));

        context.start();

        main.run();
    }

    /**
     * This uses the service loader to load all possible generate artifact objects and looks to see if they match
     * 
     * @param toRetrieve
     *            the csv columns to be parsed into a generate artifact, assumption is one will contain the packging type of the desired object.
     * @return null if no generate artifact can be found
     * @throws CSVParserException
     *             thrown if a generate artifact has no packaging type.
     */
    private static Set<String> getTrusted(final Class<?> toRetrieve) throws CSVParserException {
        final Set<String> trustedClasses = new HashSet<String>();

        final ServiceLoader<?> loader = ServiceLoader.load(toRetrieve);
        final Iterator<?> iterator = loader.iterator();

        while (iterator.hasNext()) {
            final Object artifact = iterator.next();
            trustedClasses.add(artifact.getClass().getPackage().getName());
        }

        return trustedClasses;
    }

}
