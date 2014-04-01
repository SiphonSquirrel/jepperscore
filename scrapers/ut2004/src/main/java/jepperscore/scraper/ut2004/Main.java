package jepperscore.scraper.ut2004;

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
	 *            [Message Destination Class] [Message Destination Setup] [UT2004 Console Log File]
	 *            [Hostname] [Query Port]
	 */
	public static void main(String[] args) {
		if (args.length != 5) {
			throw new RuntimeException(
					"Incorrect arguments! Need [Message Destination Class] [Message Destination Setup] [UT2004 Console Log File] [Hostname] [Query Port]");
		}

		String messageDestinationClass = args[0];
		String messageDestinationSetup = args[1];
		String logFile = args[2];
		String host = args[3];
		int queryPort = 0; // 4

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

		if (queryPort <= 0) {
			throw new RuntimeException("Could not parse port: " + args[4]);
		}

		UT2004Scraper scraper = new UT2004Scraper(messageDestination, logFile,
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
