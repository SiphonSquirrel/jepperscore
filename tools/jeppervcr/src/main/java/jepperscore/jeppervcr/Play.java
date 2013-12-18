package jepperscore.jeppervcr;

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
import jepperscore.jeppervcr.model.RecordingEntry;

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
	 * The main function.
	 *
	 * @param args
	 *            [Active MQ Connection String]
	 */
	public static void main(String[] args) {
		if (args.length != 3) {
			throw new RuntimeException(
					"Incorrect arguments! Need [Active MQ Connection String] [Input File]");
		}

		String messageDestinationClass = args[0];
		String messageDestinationSetup = args[1];
		String infile = args[2];

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
