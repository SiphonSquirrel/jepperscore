package jepperscore.scraper.bf1942;

import java.lang.reflect.InvocationTargetException;

import jepperscore.dao.IMessageDestination;
import jepperscore.scraper.common.ScraperStatus;

/**
 * This class is used to launch the scraper in stand-alone mode.
 *
 * @author Chuck
 *
 */
public class Main {

	/**
	 * The main function.
	 *
	 * @param args
	 *            [Active MQ Connection String] [BF 1942 Mod Directory]
	 *            [Hostname] [Query Port] [RCON Port] [RCON Username] [RCON
	 *            Password]
	 */
	public static void main(String[] args) {
		if (args.length != 7) {
			throw new RuntimeException(
					"Incorrect arguments! Need [Active MQ Connection String] [BF 1942 Mod Directory] [Hostname] [Query Port] [RCON Port] [RCON Username] [RCON Password]");

		}

		String messageDestinationClass = args[0];
		String messageDestinationSetup = args[1];
		String modDirectory = args[2];
		String host = args[3];
		int queryPort = 0; // 4
		int rconPort = 0; // 5
		String rconUser = args[6];
		String rconPassword = args[7];

		IMessageDestination messageDestination;
		try {
			messageDestination = (IMessageDestination) Main.class.getClassLoader().loadClass(messageDestinationClass).getConstructor(String.class).newInstance(messageDestinationSetup);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException
				| ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		try {
			queryPort = Integer.parseInt(args[4]);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Could not parse query port: " + args[4]);
		}

		try {
			rconPort = Integer.parseInt(args[5]);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Could not parse RCON port: " + args[5]);
		}

		if (queryPort <= 0) {
			throw new RuntimeException("Invalid query port: " + args[4]);
		}

		if (rconPort <= 0) {
			throw new RuntimeException("Invalid rcon port: " + args[4]);
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
