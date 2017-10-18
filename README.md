# maven_mirror_tool
A tool designed to create a complete mirror of supplied Apache Maven Dependencies, the idea is this 'mirror' can then be used as a remote M2 repository on a internet disconnected network.

## Defining Input
The tool assumes you will pass in a csv file, this requires the following entries as a minimum:
> group id,artifact id,packaging type,version

For example the following would configure the system to import version 2.19.3 of camel-jms:
> org.apache.camel,camel-jms,jar,2.19.3

### Version Ranges
The system can support version ranges as well, 
> org.apache.camel,camel-jms,jar,2.18.0,2.19.3

The above example will import the versions of the Apache camel-jms library between 2.18.0 to 2.19.3 this will generate the versions:
* 2.18.0
* 2.18.1
* 2.18.2
*  2.18.3
* 2.18.4
* 2.18.5
* 2.19.0
* 2.19.1
* 2.19.2
* 2.19.3

The system makes some assumptions, the patch and minor versions will go up to 20 if the higher level version (e.g. major, minor) is a step change. The system looks to see if the version has a valid POM before it tries to download and drops ones missing from the queue.
### Classifiers
You can supply classifier's, the import tool contains a list of common classifiers (source, sources, etc..), however some projects make use of non standard classifiers, if you supply a classifier this will generate an object with and without it. classifiers should be specified before the packaging type. 
> group id,artifact id,classifier,packaging type,version

Using our camel-jms example we would use the following:
> org.apache.camel,camel-jms,camelComponent.properties,jar,2.18.0,2.19.3

Would include the following:
* camel-jms-2.18.0.pom
* camel-jms-2.18.0.jar
* camel-jms-2.18.0-source.jar
* camel-jms-2.18.0-javadoc.jar
* camel-jms-2.18.0-camelComponent.properties.jar

## Executing Project
The system can be executed via the maven-runnable-jar module, a 'shaded' jar can be built this includes all of the required dependencies so the jar can be deployed to a machine and run. To create a runnable jar run the followig maven build command:

>  mvn clean install site

This will generate a jar called mirror-runnable-jar-0.0.1-SNAPSHOT-shade.jar