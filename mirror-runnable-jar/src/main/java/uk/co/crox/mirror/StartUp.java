package uk.co.crox.mirror;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.main.Main;

import uk.co.crox.mirror.dto.maven.artifacts.MavenGenerateArtifact;
import uk.co.crox.mirror.exceptons.csv.CSVParserException;
import uk.co.crox.mirror.interfaces.DownloadArtifact;
import uk.co.crox.mirror.interfaces.GenerateArtifact;
import uk.co.crox.mirror.interfaces.PackageArtifact;
import uk.co.crox.mirror.manifest.routes.maven.MavenManifestArtifactRoute;
import uk.co.crox.mirror.routes.csv.CSVIngestRoute;
import uk.co.crox.mirror.routes.download.DownloadArtifactRoute;
import uk.co.crox.mirror.routes.generate.GenerateArtifactRoute;

public class StartUp {

  private static final String GENERATE_ARTIFACT_QUEUE = "activemq:queue:generateDownloadArtifact";

  private static final String DOWNLOAD_ARTIFACT_QUEUE = "activemq:queue:downloadArtifact";

  private static final String PROCESS_MANIFEST_QUEUE = "activemq:queue:processManifest";
  
  private static final String LICENSE_QUEUE = "activemq:queue:license";
  
  public static void main(final String[] args) throws CSVParserException, Exception {
    if (args.length == 2) {
      final File input = new File(args[0]);
      final File output = new File(args[1]);

      startCamel(input, output);
    }
  }

  
  private static void startCamel(final File input, final File output) throws CSVParserException, Exception {

    // create a Main instance
    final Main main = new Main();
    final CamelContext context = main.getOrCreateCamelContext();

    final List<String> trustedPackages = getTrusted(GenerateArtifact.class);
    trustedPackages.addAll(getTrusted(PackageArtifact.class));
    trustedPackages.addAll(getTrusted(DownloadArtifact.class));
    trustedPackages.addAll(getTrusted(GenerateArtifact.class));
    //trustedPackages.add(MavenGenerateArtifact.class.getPackage().getName());
    trustedPackages.add(HashSet.class.getPackage().getName());
    trustedPackages.add(URI.class.getPackage().getName());

    final String brokerURL = "vm://localhost?broker.persistent=false";
    final ActiveMQConnectionFactory activeMQFactory = new ActiveMQConnectionFactory(brokerURL);
    activeMQFactory.setTrustedPackages(trustedPackages);
    
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
    
    context.startAllRoutes();
    context.start();

    main.run();
  }

  /**
   * This uses the service loader to load all possible generate artifact objects and looks to see if they match
   * 
   * @param toRetrieve
   *          the csv columns to be parsed into a generate artifact, assumption is one will contain the packging type of the desired object.
   * @return null if no generate artifact can be found
   * @throws CSVParserException
   *           thrown if a generate artifact has no packaging type.
   */
  private static List<String> getTrusted(final Class<?> toRetrieve) throws CSVParserException {
    final List<String> trustedClasses = new ArrayList<String>();

    final ServiceLoader<?> loader = ServiceLoader.load(toRetrieve);
    final Iterator<?> iterator = loader.iterator();

    while (iterator.hasNext()) {
      final Object artifact = iterator.next();
      trustedClasses.add(artifact.getClass().getPackage().getName());
    }

    return trustedClasses;
  }

}
