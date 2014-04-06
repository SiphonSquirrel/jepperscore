package jepperscore.scraper.callofduty.scraper;

import jepperscore.dao.IMessageDestination;
import jepperscore.scraper.callofduty.CodVersion;
import jepperscore.scraper.common.Scraper;
import jepperscore.scraper.common.ScraperStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This scraper works with CoD4 + mods.
 *
 * @author Chuck
 *
 */
public class CoDScraper implements Scraper, Runnable {

	/**
	 * The class logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(CoDScraper.class);

	/**
	 * The status of the scraper.
	 */
	private volatile ScraperStatus status = ScraperStatus.NotRunning;

	/**
	 * The message destination to use.
	 */
	private IMessageDestination messageDestination;

	/**
	 * The directory containing the log files.
	 */
	private String logFile;

	/**
	 * The version of Call of Duty.
	 */
	private CodVersion version;

	/**
	 * Keeps track of the current running thread.
	 */
	private volatile Thread thread;

	/**
	 * This constructor sets the Call of Duty scraper.
	 *
	 * @param messageDestination
	 *            The message destination to use.
	 * @param logFile
	 *            The directory containing the log files.
	 * @param version
	 *            The version of Call of Duty.
	 */
	public CoDScraper(IMessageDestination messageDestination, String logFile,
			CodVersion version) {
		this.messageDestination = messageDestination;
		this.logFile = logFile;
		this.version = version;
	}

	@Override
	public ScraperStatus getStatus() {
		return status;
	}

	@Override
	public void start() {
		if (thread == null) {
			status = ScraperStatus.Initializing;

			thread = new Thread(this);
			thread.setDaemon(true);
			thread.start();
		}
	}

	@Override
	public void stop() {
		status = ScraperStatus.NotRunning;

		if (thread != null) {
			thread = null;
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

}
