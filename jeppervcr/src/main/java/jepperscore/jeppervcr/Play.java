package jepperscore.jeppervcr;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import jepperscore.dao.DaoConstant;
import jepperscore.jeppervcr.model.RecordingEntry;

import org.apache.activemq.ActiveMQConnectionFactory;
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
		if (args.length != 2) {
			throw new RuntimeException(
					"Incorrect arguments! Need [Active MQ Connection String] [Input File]");
		}
		String activeMqConnection = args[0];
		String infile = args[1];

		ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(
				activeMqConnection);

		Connection conn;
		Session session;
		Topic eventTopic;

		try {
			conn = cf.createConnection();
			conn.start();

			session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
			eventTopic = session.createTopic(DaoConstant.EVENT_TOPIC);

			MessageProducer producer = session.createProducer(eventTopic);

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

						TextMessage txtMessage = session
								.createTextMessage(entry.getMessage());
						producer.send(txtMessage);
					}
				}
			}

			LOG.info("Playback finished.");

		} catch (JMSException | JAXBException | XMLStreamException e) {
			throw new RuntimeException(e);
		}
	}

}
