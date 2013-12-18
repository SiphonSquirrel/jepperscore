package jepperscore.jeppervcr;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import jepperscore.backends.activemq.ActiveMQBackendConstants;
import jepperscore.jeppervcr.model.RecordingEntry;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a main function for recording events.
 *
 * @author Chuck
 *
 */
public class Record implements MessageListener, Runnable {

	/**
	 * The logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(Record.class);

	/**
	 * The JAXB context to use.
	 */
	private JAXBContext jaxbContext;

	/**
	 * The output stream.
	 */
	private OutputStream os;

	/**
	 * The start date.
	 */
	private final DateTime startDate;

	/**
	 * The XML stream.
	 */
	private XMLStreamWriter xsw;

	/**
	 * The main function.
	 *
	 * @param args
	 *            [Active MQ Connection String]
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			throw new RuntimeException(
					"Incorrect arguments! Need [Active MQ Connection String] [Output File]");
		}
		String activeMqConnection = args[0];
		String outfile = args[1];

		ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(
				activeMqConnection);

		Connection conn;
		Session session;
		Topic eventTopic;

		try {
			conn = cf.createConnection();
			conn.start();

			session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
			eventTopic = session.createTopic(ActiveMQBackendConstants.EVENT_TOPIC);

			MessageConsumer consumer = session.createConsumer(eventTopic);

			LOG.info("Opening recording file: " + outfile);
			Record recorder = new Record(outfile);
			Runtime.getRuntime().addShutdownHook(new Thread(recorder));
			consumer.setMessageListener(recorder);
		} catch (JMSException | FileNotFoundException | JAXBException
				| XMLStreamException e) {
			throw new RuntimeException(e);
		}

		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Do nothing.
			}
		}
	}

	/**
	 * This constructor points the recorder at an output file.
	 *
	 * @param outfile
	 *            The output file.
	 * @throws JAXBException
	 *             Throw if there is a problem setting up JAXB.
	 * @throws FileNotFoundException
	 *             Thrown if the file cannot be opened.
	 * @throws XMLStreamException
	 *             Thrown if there is a problem setting up the XML
	 *             serialization.
	 */
	public Record(String outfile) throws JAXBException, FileNotFoundException,
			XMLStreamException {
		os = new FileOutputStream(outfile);
		jaxbContext = JAXBContext.newInstance(RecordingEntry.class);
		startDate = DateTime.now();
		XMLOutputFactory xof = XMLOutputFactory.newFactory();
		xsw = xof.createXMLStreamWriter(os);

		xsw.writeStartDocument("UTF-8", "1.0");
		xsw.writeCharacters("\n");

		xsw.writeStartElement("recording");
		xsw.writeAttribute("startTime", startDate.toString());
		xsw.writeCharacters("\n");
	}

	@Override
	public synchronized void onMessage(Message message) {
		if (message instanceof TextMessage) {
			TextMessage txtMessage = (TextMessage) message;

			Duration duration = new Duration(startDate, DateTime.now());
			RecordingEntry entry = new RecordingEntry();

			entry.setTimeOffset(duration.getMillis() / 1000.0f);
			try {
				entry.setMessage(txtMessage.getText());
			} catch (JMSException e) {
				LOG.error(e.getMessage(), e);
				return;
			}

			try {
				Marshaller marshaller = jaxbContext.createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
				marshaller.marshal(entry, xsw);
				xsw.flush();
			} catch (JAXBException | XMLStreamException e) {
				LOG.error(e.getMessage(), e);
				return;
			}
		}
	}

	/**
	 * The shutdown method.
	 */
	@Override
	public synchronized void run() {
		try {
			LOG.info("Finishing recording file...");

			xsw.writeEndDocument();
			os.flush();
			os.close();
		} catch (IOException | XMLStreamException e) {
			LOG.error(e.getMessage(), e);
		}
	}

}
