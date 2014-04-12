package jepperscore.scraper.sourceengine.scraper;

import jepperscore.dao.IMessageDestination;
import jepperscore.scraper.common.Scraper;
import jepperscore.scraper.common.ScraperStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceEngineScraper implements Scraper, Runnable  {

	/**
	 * The class logger.
	 */
	private static final Logger LOG = LoggerFactory
			.getLogger(SourceEngineScraper.class);

	/**
	 * The status of the scraper.
	 */
	private volatile ScraperStatus status = ScraperStatus.NotRunning;

	/**
	 * The host to query.
	 */
	private String host;

	/**
	 * The query port to use.
	 */
	private int queryPort;

	/**
	 * The log port to use.
	 */
	private int logPort;

	/**
	 * This constructor sets the ETQW scraper.
	 *
	 * @param messageDestination
	 *            The message destination to use.
	 * @param host
	 *            The hostname of the server.
	 *            @param queryPort
	 *            The query port of the server.
	 * @param logPort
	 *            The query port of the server.
	 */
	public SourceEngineScraper(IMessageDestination messageDestination,
			String host, int queryPort, int logPort) {
	}

	@Override
	public ScraperStatus getStatus() {
		return status;
	}

	@Override
	public void start() {

	}

	@Override
	public void stop() {

	}

	@Override
	public void run() {

	}

}
