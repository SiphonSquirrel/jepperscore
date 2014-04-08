package jepperscore.scraper.callofduty.scraper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import jepperscore.dao.IMessageDestination;
import jepperscore.dao.model.Alias;
import jepperscore.dao.model.Score;
import jepperscore.scraper.callofduty.CodVersion;
import jepperscore.scraper.common.Scraper;
import jepperscore.scraper.common.ScraperStatus;
import jepperscore.scraper.common.SimpleDataManager;
import jepperscore.scraper.common.query.QueryCallbackInfo;
import jepperscore.scraper.common.query.QueryClientListener;
import jepperscore.scraper.common.query.quake3.Quake3QueryClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This scraper works with CoD4 + mods.
 * 
 * @author Chuck
 * 
 */
public class CoDScraper implements Scraper, Runnable, QueryClientListener {

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
	 * The player manager to use.
	 */
	private SimpleDataManager dataManager;

	/**
	 * Keeps track of the current running thread.
	 */
	private volatile Thread thread;

	/**
	 * The server hostname.
	 */
	private String server;

	/**
	 * The query port.
	 */
	private int queryPort;

	/**
	 * The query client.
	 */
	private Quake3QueryClient queryClient;

	/**
	 * This constructor sets the Call of Duty scraper.
	 * 
	 * @param messageDestination
	 *            The message destination to use.
	 * @param logFile
	 *            The directory containing the log files.
	 * @param version
	 *            The version of Call of Duty.
	 * @param server
	 *            The server hostname.
	 * @param queryPort
	 *            The query port.
	 */
	public CoDScraper(IMessageDestination messageDestination, String logFile,
			CodVersion version, String server, int queryPort) {
		this.messageDestination = messageDestination;
		this.logFile = logFile;
		this.version = version;
		this.server = server;
		this.queryPort = queryPort;
	}

	@Override
	public ScraperStatus getStatus() {
		return status;
	}

	@Override
	public void start() {
		if (thread == null) {
			status = ScraperStatus.Initializing;

			dataManager = new SimpleDataManager(messageDestination);

			try {
				queryClient = new Quake3QueryClient(server, queryPort);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			queryClient.start();
			queryClient.registerListener("status", this);

			thread = new Thread(this);
			thread.setDaemon(true);
			thread.start();
		}
	}

	@Override
	public void stop() {
		status = ScraperStatus.NotRunning;

		if (queryClient != null) {
			queryClient.stop();
			queryClient = null;
		}

		if (thread != null) {
			thread = null;
		}
	}

	@Override
	public void run() {
		try (InputStream is = new FileInputStream(logFile)) {
			switch (version) {
			case COD4: {
				Cod4LogParser parser = new Cod4LogParser(is, messageDestination, dataManager, dataManager);
				parser.run();
				break;
			}
			default:
				throw new RuntimeException("Cannot run scraper for " + version.toString());
			}
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	@Override
	public void queryClient(QueryCallbackInfo info) {
		for (Alias player : info.getPlayers()) {
			dataManager.providePlayerRecord(player);
		}

		for (Score score : info.getScores()) {
			dataManager.provideScoreRecord(score);
		}
	}

}
