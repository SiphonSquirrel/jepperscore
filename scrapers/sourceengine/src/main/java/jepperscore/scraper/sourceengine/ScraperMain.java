package jepperscore.scraper.sourceengine;

import java.lang.reflect.InvocationTargetException;

import jepperscore.dao.IMessageDestination;
import jepperscore.scraper.common.ScraperStatus;
import jepperscore.scraper.sourceengine.scraper.SourceEngineScraper;

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
	 * Specifies the hostname of the server.
	 */
	private static final String HOSTNAME_ARG = "h";

	/**
	 * Specifies the query port of the server.
	 */
	private static final String QUERY_PORT_ARG = "p";

	/**
	 * Specifies the query port of the server.
	 */
	private static final String LOG_PORT_ARG = "l";

	/**
	 * The default query port.
	 */
	private static final String DEFAULT_QUERY_PORT = "27015";

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
		options.addOption(HOSTNAME_ARG, true,
				"Specifies the hostname of the server.");
		options.addOption(QUERY_PORT_ARG, true,
				"Specifies the query port of the server.");
		options.addOption(LOG_PORT_ARG, true,
				"Specifies the log port of the server.");

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(options, args);

		if (!cmd.hasOption(DESTINATION_CLASS_ARG)
				|| !cmd.hasOption(DESTINATION_SETUP_ARG)
				|| !cmd.hasOption(HOSTNAME_ARG) || !cmd.hasOption(LOG_PORT_ARG)) {
			throw new RuntimeException(
					"Incorrect arguments! Need -c [Message Destination Class] -s [Message Destination Setup] -h [Hostname] -l [Log Port] {-p [Query Port]}");
		}

		String messageDestinationClass = cmd
				.getOptionValue(DESTINATION_CLASS_ARG);
		String messageDestinationSetup = cmd
				.getOptionValue(DESTINATION_SETUP_ARG);
		String host = cmd.getOptionValue(HOSTNAME_ARG);
		int logPort = 0;
		int queryPort = 0;

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

		String logPortString = cmd.getOptionValue(LOG_PORT_ARG, "");
		try {
			logPort = Integer.parseInt(logPortString);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Could not parse query port: "
					+ logPortString);
		}

		if (logPort <= 0) {
			throw new RuntimeException("Could not parse port: "
					+ logPortString);
		}

		String queryPortString = cmd.getOptionValue(QUERY_PORT_ARG, DEFAULT_QUERY_PORT);
		try {
			queryPort = Integer.parseInt(queryPortString);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Could not parse query port: "
					+ queryPortString);
		}

		if (queryPort <= 0) {
			throw new RuntimeException("Could not parse port: "
					+ queryPortString);
		}

		SourceEngineScraper scraper = new SourceEngineScraper(
				messageDestination, host, queryPort, logPort);

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
