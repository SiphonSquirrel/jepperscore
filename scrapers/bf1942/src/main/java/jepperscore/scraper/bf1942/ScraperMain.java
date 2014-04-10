package jepperscore.scraper.bf1942;

import java.lang.reflect.InvocationTargetException;

import jepperscore.dao.IMessageDestination;
import jepperscore.scraper.bf1942.scraper.BF1942Scraper;
import jepperscore.scraper.common.ScraperStatus;

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
	 * Specifies BF1942 mod directory.
	 */
	private static final String MOD_DIRECTORY_ARG = "d";

	/**
	 * Specifies the hostname of the server.
	 */
	private static final String HOSTNAME_ARG= "h";

	/**
	 * Specifies the query port of the server.
	 */
	private static final String QUERY_PORT_ARG = "p";

	/**
	 * Specifies the RCON port of the server.
	 */
	private static final String RCON_PORT_ARG = "r";

	/**
	 * Specifies the RCON username.
	 */
	private static final String RCON_USERNAME_ARG = "u";

	/**
	 * Specifies the RCON password.
	 */
	private static final String RCON_PASSWORD_ARG = "P";

	/**
	 * The default query port.
	 */
	private static final String DEFAULT_QUERY_PORT  = "22000";

	/**
	 * The default rcon port.
	 */
	private static final String DEFAULT_RCON_PORT  = "4711";

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
		options.addOption(MOD_DIRECTORY_ARG, true, "Specifies BF1942 mod directory.");
		options.addOption(HOSTNAME_ARG, true, "Specifies the hostname of the server.");
		options.addOption(QUERY_PORT_ARG, true, "Specifies the query port of the server.");
		options.addOption(RCON_PORT_ARG, true, "Specifies the RCON port of the server.");
		options.addOption(RCON_USERNAME_ARG, true, "Specifies the RCON Username of the server.");
		options.addOption(RCON_PASSWORD_ARG, true, "Specifies the RCON Password of the server.");

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse( options, args);

		if (!cmd.hasOption(DESTINATION_CLASS_ARG) || !cmd.hasOption(DESTINATION_SETUP_ARG) || !cmd.hasOption(MOD_DIRECTORY_ARG) || !cmd.hasOption(HOSTNAME_ARG)) {
			throw new RuntimeException(
					"Incorrect arguments! Need -c [Message Destination Class] -s [Message Destination Setup] -d [BF 1942 Mod Directory] -h [Hostname] -u [RCON Username] -P [RCON Password] {-p [Query Port]} {-r [RCON Port]}");
		}
		String messageDestinationClass = cmd.getOptionValue(DESTINATION_CLASS_ARG);
		String messageDestinationSetup = cmd.getOptionValue(DESTINATION_SETUP_ARG);
		String modDirectory = cmd.getOptionValue(MOD_DIRECTORY_ARG);
		String host = cmd.getOptionValue(HOSTNAME_ARG);
		String rconUser = cmd.getOptionValue(RCON_USERNAME_ARG);
		String rconPassword = cmd.getOptionValue(RCON_PASSWORD_ARG);

		int queryPort = 0;
		int rconPort = 0;

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

		String rconPortString = cmd.getOptionValue(RCON_PORT_ARG, DEFAULT_RCON_PORT);
		try {
			rconPort = Integer.parseInt(rconPortString);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Could not parse RCON port: " + rconPortString);
		}

		if (rconPort <= 0) {
			throw new RuntimeException("Could not parse port: " + rconPortString);
		}

		BF1942Scraper scraper = new BF1942Scraper(messageDestination,
				modDirectory, host, queryPort, rconPort, rconUser, rconPassword);

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
