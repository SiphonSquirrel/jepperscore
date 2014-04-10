package jepperscore.scraper.etl;


import java.lang.reflect.InvocationTargetException;

import jepperscore.dao.IMessageDestination;
import jepperscore.scraper.common.ScraperStatus;
import jepperscore.scraper.etl.scraper.ETLScraper;

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
	 * This is the default query port.
	 */
	private static final String DEFAULT_QUERY_PORT = "27960";

	/**
	 * This is the default hostname.
	 */
	private static final String DEFAULT_SERVER_HOST = "localhost";

	/**
	 * Specifies the destination class.
	 */
	private static final String DESTINATION_CLASS_ARG = "c";

	/**
	 * Specifies the destination class setup.
	 */
	private static final String DESTINATION_SETUP_ARG = "s";

	/**
	 * Specifies CoD's log location.
	 */
	private static final String CONSOLE_LOG_ARG = "l";

	/**
	 * The host to query.
	 */
	private static final String SERVER_HOST = "h";

	/**
	 * The query port.
	 */
	private static final String QUERY_PORT = "p";

	/**
	 * The main function.
	 *
	 * @param args
	 *            See option setup.
	 * @throws ParseException
	 *             Exception throw from parsing problems.
	 */
	public static void main(String[] args) throws ParseException {
		Options options = new Options();

		options.addOption(DESTINATION_CLASS_ARG, true,
				"Specifies the destination class.");
		options.addOption(DESTINATION_SETUP_ARG, true,
				"Specifies the destination class setup.");
		options.addOption(CONSOLE_LOG_ARG, true,
				"Specifies ETL's log location.");

		options.addOption(SERVER_HOST, true, "Specifies the server hostname.");
		options.addOption(QUERY_PORT, true, "Specifies the query port.");

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(options, args);

		if (!cmd.hasOption(DESTINATION_CLASS_ARG)
				|| !cmd.hasOption(DESTINATION_SETUP_ARG)
				|| !cmd.hasOption(CONSOLE_LOG_ARG)) {
			throw new RuntimeException(
					"Incorrect arguments! Need -c [Message Destination Class] -s [Message Destination Setup] -l [ETL's Server Log] {-h [Server Hostname]} {-q [Query Port]}");
		}

		String messageDestinationClass = cmd
				.getOptionValue(DESTINATION_CLASS_ARG);
		String messageDestinationSetup = cmd
				.getOptionValue(DESTINATION_SETUP_ARG);
		String logFile = cmd.getOptionValue(CONSOLE_LOG_ARG);
		String server = cmd.getOptionValue(SERVER_HOST, DEFAULT_SERVER_HOST);
		int queryPort = Integer.parseInt(cmd.getOptionValue(QUERY_PORT, DEFAULT_QUERY_PORT));

		IMessageDestination messageDestination;
		try {
			messageDestination = (IMessageDestination) ScraperMain.class
					.getClassLoader().loadClass(messageDestinationClass)
					.getConstructor(String.class)
					.newInstance(messageDestinationSetup);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException
				| ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		ETLScraper scraper = new ETLScraper(messageDestination, logFile,
				server, queryPort);

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
