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
	 * The main function.
	 * @param args [Active MQ Connection String] [BF 1942 Log Directory] [Hostname] [Query Port] [RCON Port] [RCON Username] [RCON Password]
	 */
	public static void main(String[] args) {
		if (args.length != 7) {
			throw new RuntimeException("Incorrect arguments! Need [Active MQ Connection String] [BF 1942 Log Directory] [Hostname] [Query Port] [RCON Port] [RCON Username] [RCON Password]");

		}

		String activeMqConnection = args[0];
		String logDirectory = args[1];
		String host = args[2];
		int queryPort = 0; //3
		int rconPort = 0; //4
		String rconUser = args[5];
		String rconPassword = args[6];

		try {
			queryPort = Integer.parseInt(args[3]);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Could not parse query port: " + args[3]);
		}

		try {
			rconPort = Integer.parseInt(args[4]);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Could not parse RCON port: " + args[4]);
		}

		if (queryPort <= 0) {
			throw new RuntimeException("Could not parse port: " + args[3]);
		}

		try {
			BF1942Scraper scraper = new BF1942Scraper(activeMqConnection,
					logDirectory, host, queryPort, rconPort, rconUser, rconPassword);

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
