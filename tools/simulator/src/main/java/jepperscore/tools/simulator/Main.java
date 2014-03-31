package jepperscore.tools.simulator;

import java.lang.reflect.InvocationTargetException;

import jepperscore.dao.IMessageDestination;
import jepperscore.dao.MessageRelay;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * This class handles the playback of recordings.
 *
 * @author Chuck
 *
 */
public class Main {

	/**
	 * Specifies the destination class.
	 */
	private static final String DESTINATION_CLASS_ARG = "c";
	
	/**
	 * Specifies the destination class setup.
	 */
	private static final String DESTINATION_SETUP_ARG = "s";
	
	/**
	 * The main function.
	 *
	 * @param args
	 *            [Active MQ Connection String]
	 * @throws ParseException Exception throw from parsing problems.
	 */
	public static void main(String[] args) throws ParseException {
		Options options = new Options();
		
		options.addOption(DESTINATION_CLASS_ARG, true, "Specifies the destination class.");
		options.addOption(DESTINATION_SETUP_ARG, true, "Specifies the destination class setup.");
		
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse( options, args);
		
		if (!cmd.hasOption(DESTINATION_CLASS_ARG) || !cmd.hasOption(DESTINATION_SETUP_ARG)) {
			throw new RuntimeException(
					"Incorrect arguments! Need -c [Message Destination Class] -s [Message Destination Setup]");
		}
		
		String messageDestinationClass = cmd.getOptionValue(DESTINATION_CLASS_ARG);
		String messageDestinationSetup = cmd.getOptionValue(DESTINATION_SETUP_ARG);

		IMessageDestination messageDestination;
		try {
			messageDestination = (IMessageDestination) Main.class
					.getClassLoader().loadClass(messageDestinationClass)
					.getConstructor(String.class)
					.newInstance(messageDestinationSetup);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException
				| ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		SimulatorSource simulator = new SimulatorSource();
		
		new MessageRelay(simulator, messageDestination);
		
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Do nothing.
			}
		}
	}

}
