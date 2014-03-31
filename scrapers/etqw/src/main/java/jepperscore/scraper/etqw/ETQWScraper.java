package jepperscore.scraper.etqw;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import jepperscore.dao.IMessageDestination;
import jepperscore.dao.model.Score;
import jepperscore.scraper.common.Scraper;
import jepperscore.scraper.common.ScraperStatus;
import jepperscore.scraper.common.SimpleDataManager;
import jepperscore.scraper.common.query.QueryCallbackInfo;
import jepperscore.scraper.common.query.QueryClientListener;
import jepperscore.scraper.common.query.idtech.IdTech4QueryClient;
import jepperscore.scraper.common.query.idtech.IdTechScoreMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This scraper works for ETQW.
 *
 * @author Chuck
 *
 */
public class ETQWScraper implements Scraper, Runnable {

	/**
	 * This is the listener for info query.
	 *
	 * @author Chuck
	 *
	 */
	private class ETQWInfoQueryListener implements QueryClientListener {
		@Override
		public void queryClient(QueryCallbackInfo info) {
			Collection<Score> scores = info.getScores();
			if (scores != null) {
				for (Score s : scores) {
					dataManager.provideScoreRecord(s);
				}
			}
		}

	}

	/**
	 * The class logger.
	 */
	private static final Logger LOG = LoggerFactory
			.getLogger(ETQWScraper.class);

	/**
	 * The status of the scraper.
	 */
	private volatile ScraperStatus status = ScraperStatus.NotRunning;

	/**
	 * The log file to watch.
	 */
	private String logFile;

	/**
	 * The host to query.
	 */
	private String host;

	/**
	 * The query port to use.
	 */
	private int queryPort;

	/**
	 * Keeps track of the current running thread.
	 */
	private volatile Thread thread;

	/**
	 * The query client.
	 */
	private IdTech4QueryClient queryClient;

	/**
	 * The message destination.
	 */
	private final IMessageDestination messageDestination;

	/**
	 * The player manager to use.
	 */
	private volatile SimpleDataManager dataManager;

	/**
	 * This constructor sets the ETQW scraper.
	 *
	 * @param messageDestination
	 *            The message destination to use.
	 * @param logFile
	 *            The log file.
	 * @param host
	 *            The hostname of the server.
	 * @param queryPort
	 *            The query port of the server.
	 */
	public ETQWScraper(@Nonnull IMessageDestination messageDestination,
			@Nonnull String logFile, @Nonnull String host,
			@Nonnegative int queryPort) {
		this.logFile = logFile;
		this.host = host;
		this.queryPort = queryPort;
		this.messageDestination = messageDestination;
	}

	@Override
	public ScraperStatus getStatus() {
		return status;
	}

	@Override
	public synchronized void start() {
		if (thread == null) {
			status = ScraperStatus.Initializing;

			if (!new File(logFile).exists()) {
				LOG.error("Log directory does not exist.");
				status = ScraperStatus.InError;
				return;
			}

			dataManager = new SimpleDataManager(messageDestination);

			try {
				LOG.info("Starting query client on {}:{}", new Object[] { host,
						queryPort });
				queryClient = new IdTech4QueryClient(host, queryPort,
						dataManager);
				queryClient.setScoreMode(IdTechScoreMode.Experience);
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
				status = ScraperStatus.InError;
				return;
			}

			ETQWInfoQueryListener queryListener = new ETQWInfoQueryListener();
			queryClient.registerListener("infoEx", queryListener);

			queryClient.start();

			thread = new Thread(this);
			thread.setDaemon(true);
			thread.start();
		}
	}

	@Override
	public synchronized void stop() {
		status = ScraperStatus.NotRunning;

		if (thread != null) {
			queryClient.stop();
			thread = null;
		}
	}

	@Override
	public void run() {
		try (InputStream is = new FileInputStream(logFile)) {
			ETQWLogParser parser = new ETQWLogParser(is, messageDestination, dataManager, dataManager);
			parser.run();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}
}
