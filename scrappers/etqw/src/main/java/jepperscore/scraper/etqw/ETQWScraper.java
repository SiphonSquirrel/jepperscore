package jepperscore.scraper.etqw;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.xml.bind.JAXBException;

import jepperscore.dao.DaoConstant;
import jepperscore.scraper.common.PlayerManager;
import jepperscore.scraper.common.Scraper;
import jepperscore.scraper.common.ScraperStatus;
import jepperscore.scraper.common.query.QueryCallbackInfo;
import jepperscore.scraper.common.query.QueryClient;
import jepperscore.scraper.common.query.QueryClientListener;
import jepperscore.scraper.common.query.idtech.IdTech4QueryClient;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ETQWScraper implements Scraper, Runnable {

	private class ETQWInfoQueryListener implements QueryClientListener {

		/**
		 * This producer is used to send messages from the query client.
		 */
		private MessageProducer producer;

		/**
		 * Default constructor.
		 *
		 * @throws JMSException
		 *             When there is a problem creating the producer.
		 */
		public ETQWInfoQueryListener() throws JMSException {
			producer = session.createProducer(eventTopic);
		}

		@Override
		public void queryClient(QueryCallbackInfo info) {
			// TODO Auto-generated method stub
			
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
	 * The log directory to watch.
	 */
	private String logDirectory;

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
	private QueryClient queryClient;

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
	private PlayerManager playerManager;

	/**
	 * This constructor sets the ETQW scraper.
	 * 
	 * @param activeMqConnection
	 *            The connection string to use for ActiveMQ.
	 * @param logDirectory
	 *            The directory containing the log files.
	 * @param host
	 *            The hostname of the server.
	 * @param queryPort
	 *            The query port of the server.
	 * @throws JMSException
	 *             When a problem occurs connecting to ActiveMQ.
	 * @throws JAXBException
	 *             When a problem setting up the JAXB Context.
	 */
	public ETQWScraper(@Nonnull String activeMqConnection,
			@Nonnull String logDirectory, @Nonnull String host,
			@Nonnegative int queryPort) throws JMSException {
		this.logDirectory = logDirectory;
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

			if (!new File(logDirectory).exists()) {
				LOG.error("Log directory does not exist.");
				status = ScraperStatus.InError;
				return;
			}

			try {
				playerManager = new PlayerManager(session,
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
						playerManager);
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
				status = ScraperStatus.InError;
				return;
			}
			try {
				ETQWInfoQueryListener queryListener = new ETQWInfoQueryListener();
				queryClient.registerListener("info", queryListener);
			} catch (JMSException e) {
				LOG.error(e.getMessage(), e);
				status = ScraperStatus.InError;
				return;
			}

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
		// TODO Auto-generated method stub
		
	}
}
