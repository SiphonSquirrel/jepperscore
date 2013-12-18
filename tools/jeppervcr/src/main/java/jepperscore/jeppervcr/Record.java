package jepperscore.jeppervcr;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import jepperscore.dao.IMessageCallback;
import jepperscore.dao.IMessageSource;
import jepperscore.dao.transport.TransportMessage;
import jepperscore.jeppervcr.model.RecordingEntry;

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
public class Record implements IMessageCallback, Runnable {

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
					"Incorrect arguments! Need [Message Destination Class] [Message Destination Setup] [Output File]");
		}
		String messageSourceClass = args[0];
		String messageSourceSetup = args[1];
		String outfile = args[2];

		IMessageSource messageSource;
		try {
			messageSource = (IMessageSource) Play.class.getClassLoader()
					.loadClass(messageSourceClass).getConstructor(String.class)
					.newInstance(messageSourceSetup);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException
				| ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		try {
			LOG.info("Opening recording file: " + outfile);
			Record recorder = new Record(outfile);
			Runtime.getRuntime().addShutdownHook(new Thread(recorder));

			messageSource.registerCallback(recorder);
		} catch (FileNotFoundException | JAXBException
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

	@Override
	public void onMessage(TransportMessage message) {
		Duration duration = new Duration(startDate, DateTime.now());
		RecordingEntry entry = new RecordingEntry();

		entry.setTimeOffset(duration.getMillis() / 1000.0f);
		entry.setMessage(message);

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
