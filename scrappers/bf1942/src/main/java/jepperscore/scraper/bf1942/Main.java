package jepperscore.scraper.bf1942;

import javax.jms.JMSException;
import javax.xml.bind.JAXBException;

import jepperscore.scraper.common.ScraperStatus;

/**
 * This class is used to launch the scraper in stand-alone mode.
 *
 * @author Chuck
 *
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 4) {
			System.err
					.println("Incorrect arguments! Need [Active MQ Connection String] [BF 1942 Log Directory] [Hostname] [QueryPort]");
			System.exit(1);
		}

		String activeMqConnection = args[0];
		String logDirectory = args[1];
		String host = args[2];
		int port = 0;

		try {
			port = Integer.parseInt(args[3]);
		} catch (NumberFormatException e) {
			System.err.println("Could not parse port: " + args[3]);
			System.exit(4);
		}

		if (port <= 0) {
			System.err.println("Could not parse port: " + args[3]);
			System.exit(4);
		}

		try {
			BF1942Scraper scraper = new BF1942Scraper(activeMqConnection,
					logDirectory, host, port);

			scraper.start();
			do {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					break;
				}
			} while (scraper.getStatus() != ScraperStatus.NotRunning);
		} catch (JMSException | JAXBException e) {
			System.err.println(e.getMessage());
		}
	}

}
