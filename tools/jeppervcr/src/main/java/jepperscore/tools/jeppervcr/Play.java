package jepperscore.tools.jeppervcr;

import java.lang.reflect.InvocationTargetException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import jepperscore.dao.IMessageDestination;
import jepperscore.tools.jeppervcr.model.RecordingEntry;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles the playback of recordings.
 *
 * @author Chuck
 *
 */
public class Play {

	/**
	 * The logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(Play.class);

	/**
	 * Specifies the destination class.
	 */
	private static final String DESTINATION_CLASS_ARG = "c";
	
	/**
	 * Specifies the destination class setup.
	 */
	private static final String DESTINATION_SETUP_ARG = "s";
	
	/**
	 * Specifies the input file.
	 */
	private static final String INPUT_FILE_ARG = "i";
	
	/**
	 * The main function.
	 *
	 * @param args
	 *            [Active MQ Connection String]
	 * @throws ParseException Exception throw from parsing problems.
	 */
	public static void main(String[] args) throws ParseException {
		Options options = new Options();
		
		options.addOption(DESTINATION_CLASS_ARG, true, "Specifies the destination class.");
		options.addOption(DESTINATION_SETUP_ARG, true, "Specifies the destination class setup.");
		options.addOption(INPUT_FILE_ARG, true, "Specifies the input file.");
		
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse( options, args);
		
		if (!cmd.hasOption(DESTINATION_CLASS_ARG) || !cmd.hasOption(DESTINATION_SETUP_ARG) ||
				!cmd.hasOption(INPUT_FILE_ARG)) {
			throw new RuntimeException(
					"Incorrect arguments! Need -c [Message Destination Class] -s [Message Destination Setup] -i [Input File]");
		}
		
		String messageDestinationClass = cmd.getOptionValue(DESTINATION_CLASS_ARG);
		String messageDestinationSetup = cmd.getOptionValue(DESTINATION_SETUP_ARG);
		String infile = cmd.getOptionValue(INPUT_FILE_ARG);

		IMessageDestination messageDestination;
		try {
			messageDestination = (IMessageDestination) Play.class
					.getClassLoader().loadClass(messageDestinationClass)
					.getConstructor(String.class)
					.newInstance(messageDestinationSetup);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException
				| ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		try {
			LOG.info("Opening recording file: " + infile);

			StreamSource is = new StreamSource(infile);

			XMLInputFactory xif = XMLInputFactory.newFactory();
			XMLStreamReader xsr = xif.createXMLStreamReader(is);

			JAXBContext jaxbContext = JAXBContext
					.newInstance(RecordingEntry.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

			DateTime start = DateTime.now();

			while (xsr.hasNext()) {
				if (xsr.next() == XMLStreamConstants.START_ELEMENT) {
					if (xsr.getLocalName().equals("recordingEntry")) {
						RecordingEntry entry = (RecordingEntry) unmarshaller
								.unmarshal(xsr);

						Duration duration = new Duration(start, DateTime.now());
						float until = entry.getTimeOffset()
								- (duration.getMillis() / 1000.0f);
						if (until > 0) {
							try {
								Thread.sleep((long) (until * 1000));
							} catch (InterruptedException e) {
								// Do nothing.
							}
						}

						messageDestination.sendMessage(entry.getMessage());
					}
				}
			}

			LOG.info("Playback finished.");

		} catch (JAXBException | XMLStreamException e) {
			throw new RuntimeException(e);
		}
	}

}
