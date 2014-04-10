package jepperscore.scraper.etqw;

import java.lang.reflect.InvocationTargetException;

import jepperscore.dao.IMessageDestination;
import jepperscore.scraper.common.ScraperStatus;
import jepperscore.scraper.etqw.scraper.ETQWScraper;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * This class is used to launch the scraper in stand-alone mode.
 *
 * @author Chuck
 *
 */
public class ScraperMain {

	/**
	 * Specifies the destination class.
	 */
	private static final String DESTINATION_CLASS_ARG = "c";

	/**
	 * Specifies the destination class setup.
	 */
	private static final String DESTINATION_SETUP_ARG = "s";

	/**
	 * Specifies ETQW's log directory.
	 */
	private static final String LOG_DIRECTORY_ARG = "l";

	/**
	 * Specifies the hostname of the server.
	 */
	private static final String HOSTNAME_ARG= "h";

	/**
	 * Specifies the query port of the server.
	 */
	private static final String QUERY_PORT_ARG = "p";

	/**
	 * The default query port.
	 */
	private static final String DEFAULT_QUERY_PORT  = "27733";

	/**
	 * The main function.
	 *
	 * @param args See option setup.
	 * @throws ParseException Exception throw from parsing problems.
	 */
	public static void main(String[] args) throws ParseException {
		Options options = new Options();

		options.addOption(DESTINATION_CLASS_ARG, true, "Specifies the destination class.");
		options.addOption(DESTINATION_SETUP_ARG, true, "Specifies the destination class setup.");
		options.addOption(LOG_DIRECTORY_ARG, true, "Specifies ETQW's log directory.");
		options.addOption(HOSTNAME_ARG, true, "Specifies the hostname of the server.");
		options.addOption(QUERY_PORT_ARG, true, "Specifies the query port of the server.");

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse( options, args);

		if (!cmd.hasOption(DESTINATION_CLASS_ARG) || !cmd.hasOption(DESTINATION_SETUP_ARG) || !cmd.hasOption(LOG_DIRECTORY_ARG) || !cmd.hasOption(HOSTNAME_ARG)) {
			throw new RuntimeException(
					"Incorrect arguments! Need -c [Message Destination Class] -s [Message Destination Setup] -l [ETQW Log Directory] -h [Hostname] {-p [Query Port]}");
		}

		String messageDestinationClass = cmd.getOptionValue(DESTINATION_CLASS_ARG);
		String messageDestinationSetup = cmd.getOptionValue(DESTINATION_SETUP_ARG);
		String logDirectory = cmd.getOptionValue(LOG_DIRECTORY_ARG);
		String host = cmd.getOptionValue(HOSTNAME_ARG);
		int queryPort = 0;

		IMessageDestination messageDestination;
		try {
			messageDestination = (IMessageDestination) ScraperMain.class.getClassLoader().loadClass(messageDestinationClass).getConstructor(String.class).newInstance(messageDestinationSetup);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException
				| ClassNotFoundException e) {
			throw new RuntimeException(e);
		}


		String queryPortString = cmd.getOptionValue(QUERY_PORT_ARG, DEFAULT_QUERY_PORT);
		try {
			queryPort = Integer.parseInt(queryPortString);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Could not parse query port: " + queryPortString);
		}

		if (queryPort <= 0) {
			throw new RuntimeException("Could not parse port: " + queryPortString);
		}

		ETQWScraper scraper = new ETQWScraper(messageDestination, logDirectory,
				host, queryPort);

		scraper.start();
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				break;
			}
		} while ((scraper.getStatus() != ScraperStatus.NotRunning)
				&& (scraper.getStatus() != ScraperStatus.InError));
	}

}
