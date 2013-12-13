package jepperscore.scraper.etqw;

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
	 *            [Active MQ Connection String] [ETQW Log Directory] [Hostname]
	 *            [Query Port]
	 */
	public static void main(String[] args) {
		if (args.length != 4) {
			throw new RuntimeException(
					"Incorrect arguments! Need [Active MQ Connection String] [ETQW Log Directory] [Hostname] [Query Port]");
		}

		String messageDestinationClass = args[0];
		String messageDestinationSetup = args[1];
		String logDirectory = args[2];
		String host = args[3];
		int queryPort = 0; // 4

		try {
			queryPort = Integer.parseInt(args[4]);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Could not parse query port: " + args[4]);
		}

		if (queryPort <= 0) {
			throw new RuntimeException("Could not parse port: " + args[4]);
		}

		IMessageDestination messageDestination;
		try {
			messageDestination = (IMessageDestination) Main.class.getClassLoader().loadClass(messageDestinationClass).getConstructor(String.class).newInstance(messageDestinationSetup);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException
				| ClassNotFoundException e) {
			throw new RuntimeException(e);
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
