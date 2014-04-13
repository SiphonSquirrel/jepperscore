package jepperscore.scraper.sourceengine.scraper;

import java.io.IOException;

import jepperscore.dao.IMessageDestination;
import jepperscore.scraper.common.Scraper;
import jepperscore.scraper.common.ScraperStatus;
import jepperscore.scraper.common.SimpleDataManager;
import jepperscore.scraper.common.query.sourceengine.SourceEngineQueryClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This scraper works with Source Engine games. Currently tested with TF2.
 *
 * @author Chuck
 *
 */
public class SourceEngineScraper implements Scraper {

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
	 * The message destination.
	 */
	private IMessageDestination messageDestination;

	/**
	 * The log parser.
	 */
	private SourceEngineLogParser logParser;

	/**
	 * The query client.
	 */
	private SourceEngineQueryClient queryClient;

	/**
	 * The data manager.
	 */
	private SimpleDataManager dataManager;

	/**
	 * This constructor sets the ETQW scraper.
	 *
	 * @param messageDestination
	 *            The message destination to use.
	 * @param host
	 *            The hostname of the server.
	 * @param queryPort
	 *            The query port of the server.
	 * @param logPort
	 *            The query port of the server.
	 */
	public SourceEngineScraper(IMessageDestination messageDestination,
			String host, int queryPort, int logPort) {
		this.messageDestination = messageDestination;
		this.host = host;
		this.queryPort = queryPort;
		this.logPort = logPort;
		dataManager = new SimpleDataManager(messageDestination);
	}

	@Override
	public ScraperStatus getStatus() {
		return status;
	}

	@Override
	public void start() {
		if (status == ScraperStatus.NotRunning) {
			try {
				logParser = new SourceEngineLogParser(logPort,
						messageDestination, dataManager, dataManager);
				logParser.start();

				queryClient = new SourceEngineQueryClient(host, queryPort);
				queryClient.start();
			} catch (IOException e) {
				status = ScraperStatus.InError;
				if (logParser != null) {
					logParser.stop();
					logParser = null;
				}

				if (queryClient != null) {
					queryClient.stop();
					queryClient = null;
				}
				LOG.error(e.getMessage(), e);
			}

		}
	}

	@Override
	public void stop() {
		status = ScraperStatus.NotRunning;
		if (logParser != null) {
			logParser.stop();
			logParser = null;
		}

		if (queryClient != null) {
			queryClient.stop();
			queryClient = null;
		}
	}

}
