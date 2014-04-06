package jepperscore.scraper.callofduty;

import java.lang.reflect.InvocationTargetException;

import jepperscore.dao.IMessageDestination;
import jepperscore.scraper.callofduty.scraper.CoDScraper;

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
	 * Specifies CoD's log location.
	 */
	private static final String CONSOLE_LOG_ARG = "l";

	/**
	 * Specifies the version of Call of Duty.
	 */
	private static final String COD_VERSION_ARG = "v";

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
				"Specifies CoD's log location.");
		options.addOption(COD_VERSION_ARG, true,
				"Specifies the version of Call of Duty. Values: "
						+ CoDConstants.SUPPORTED_VERSIONS);

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(options, args);

		if (!cmd.hasOption(DESTINATION_CLASS_ARG)
				|| !cmd.hasOption(DESTINATION_SETUP_ARG)
				|| !cmd.hasOption(CONSOLE_LOG_ARG)
				|| !cmd.hasOption(COD_VERSION_ARG)) {
			throw new RuntimeException(
					"Incorrect arguments! Need -c [Message Destination Class] -s [Message Destination Setup] -c [CoD Server Log] -v [Cod Version]");
		}

		String messageDestinationClass = cmd
				.getOptionValue(DESTINATION_CLASS_ARG);
		String messageDestinationSetup = cmd
				.getOptionValue(DESTINATION_SETUP_ARG);
		String logFile = cmd.getOptionValue(CONSOLE_LOG_ARG);
		CodVersion version = CodVersion.valueOf(cmd.getOptionValue(COD_VERSION_ARG));

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

		CoDScraper scraper = new CoDScraper(messageDestination, logFile,
				version);

		scraper.start();
	}

}
