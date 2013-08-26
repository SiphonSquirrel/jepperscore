package jepperscore.scraper.etqw;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;

import jepperscore.dao.DaoConstant;
import jepperscore.dao.model.Score;
import jepperscore.scraper.common.ActiveMQDataManager;
import jepperscore.scraper.common.Scraper;
import jepperscore.scraper.common.ScraperStatus;
import jepperscore.scraper.common.query.QueryCallbackInfo;
import jepperscore.scraper.common.query.QueryClientListener;
import jepperscore.scraper.common.query.idtech.IdTech4QueryClient;
import jepperscore.scraper.common.query.idtech.IdTechScoreMode;

import org.apache.activemq.ActiveMQConnectionFactory;
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
	 * The ActiveMQ connection.
	 */
	private final Connection conn;

	/**
	 * The ActiveMQ session.
	 */
	private final Session session;

	/**
	 * The ActiveMQ topic.
	 */
	private final Topic eventTopic;

	/**
	 * The player manager to use.
	 */
	private volatile ActiveMQDataManager dataManager;

	/**
	 * This constructor sets the ETQW scraper.
	 *
	 * @param activeMqConnection
	 *            The connection string to use for ActiveMQ.
	 * @param logFile
	 *            The log file.
	 * @param host
	 *            The hostname of the server.
	 * @param queryPort
	 *            The query port of the server.
	 * @throws JMSException
	 *             When a problem occurs connecting to ActiveMQ.
	 */
	public ETQWScraper(@Nonnull String activeMqConnection,
			@Nonnull String logFile, @Nonnull String host,
			@Nonnegative int queryPort) throws JMSException {
		this.logFile = logFile;
		this.host = host;
		this.queryPort = queryPort;

		ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(
				activeMqConnection);
		conn = cf.createConnection();
		conn.start();

		session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
		eventTopic = session.createTopic(DaoConstant.EVENT_TOPIC);
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

			try {
				dataManager = new ActiveMQDataManager(session,
						session.createProducer(eventTopic));
			} catch (JMSException e) {
				LOG.error(e.getMessage(), e);
				status = ScraperStatus.InError;
				return;
			}

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

		if (session != null) {
			try {
				session.close();
			} catch (JMSException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public void run() {
		try (InputStream is = new FileInputStream(logFile)) {
			ETQWLogParser parser = new ETQWLogParser(is, session, session.createProducer(eventTopic), dataManager);
			parser.run();
		} catch (IOException | JMSException e) {
			LOG.error(e.getMessage(), e);
		}
	}
}
