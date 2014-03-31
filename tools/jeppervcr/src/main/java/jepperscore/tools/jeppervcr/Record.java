package jepperscore.tools.jeppervcr;

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
	 * Specifies the source class.
	 */
	private static final String SOURCE_CLASS_ARG = "c";
	
	/**
	 * Specifies the source class setup.
	 */
	private static final String SOURCE_SETUP_ARG = "s";
	
	/**
	 * Specifies the output file.
	 */
	private static final String OUTPUT_FILE_ARG = "o";
	
	/**
	 * The main function.
	 *
	 * @param args
	 *            [Active MQ Connection String]
	 * @throws ParseException Exception throw from parsing problems.
	 */
	public static void main(String[] args) throws ParseException {
		Options options = new Options();
		
		options.addOption(SOURCE_CLASS_ARG, true, "Specifies the source class.");
		options.addOption(SOURCE_SETUP_ARG, true, "Specifies the source class setup.");
		options.addOption(OUTPUT_FILE_ARG, true, "Specifies the output file");
		
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse( options, args);
		
		if (!cmd.hasOption(SOURCE_CLASS_ARG) || !cmd.hasOption(SOURCE_SETUP_ARG)) {
			throw new RuntimeException(
					"Incorrect arguments! Need -c [Message Source Class] -s [Message Source Setup] -o [Output File]");
		}
		
		String messageSourceClass = cmd.getOptionValue(SOURCE_CLASS_ARG);
		String messageSourceSetup = cmd.getOptionValue(SOURCE_SETUP_ARG);
		String outfile = cmd.getOptionValue(OUTPUT_FILE_ARG);

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
