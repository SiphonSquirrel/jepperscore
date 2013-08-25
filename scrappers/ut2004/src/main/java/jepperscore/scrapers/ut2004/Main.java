package jepperscore.scrapers.ut2004;

import javax.jms.JMSException;

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
	 *            [Active MQ Connection String] [UT2004 Console Log File]
	 *            [Hostname] [Query Port]
	 */
	public static void main(String[] args) {
		if (args.length != 4) {
			throw new RuntimeException(
					"Incorrect arguments! Need [Active MQ Connection String] [UT2004 Console Log File] [Hostname] [Query Port]");
		}

		String activeMqConnection = args[0];
		String logFile = args[1];
		String host = args[2];
		int queryPort = 0; // 3

		try {
			queryPort = Integer.parseInt(args[3]);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Could not parse query port: " + args[3]);
		}

		if (queryPort <= 0) {
			throw new RuntimeException("Could not parse port: " + args[3]);
		}

		try {
			UT2004Scraper scraper = new UT2004Scraper(activeMqConnection,
					logFile, host, queryPort);

			scraper.start();
			do {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					break;
				}
			} while ((scraper.getStatus() != ScraperStatus.NotRunning)
					&& (scraper.getStatus() != ScraperStatus.InError));
		} catch (JMSException e) {
			System.err.println(e.getMessage());
		}
	}

}
