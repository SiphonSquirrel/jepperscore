package jepperscore.scraper.bf1942;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.TreeSet;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import jepperscore.dao.DaoConstant;
import jepperscore.dao.model.Alias;
import jepperscore.dao.model.Round;
import jepperscore.dao.model.Score;
import jepperscore.dao.model.ServerMetadata;
import jepperscore.dao.transport.TransportMessage;
import jepperscore.scraper.common.Scraper;
import jepperscore.scraper.common.ScraperStatus;
import jepperscore.scraper.common.query.QueryCallbackInfo;
import jepperscore.scraper.common.query.QueryClient;
import jepperscore.scraper.common.query.QueryClientListener;
import jepperscore.scraper.common.query.gamespy.GamespyQueryClient;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This scraper works for BF1942.
 *
 * @author Chuck
 *
 */
public class BF1942Scraper implements Scraper, Runnable {

	/**
	 * This class listens to the query client.
	 *
	 * @author Chuck
	 *
	 */
	private class BF1942InfoQueryListener implements QueryClientListener {

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
		public BF1942InfoQueryListener() throws JMSException {
			producer = session.createProducer(eventTopic);

		}

		@Override
		public void queryClient(QueryCallbackInfo info) {
			try {
				Marshaller marshaller = jaxbContext.createMarshaller();

				ServerMetadata serverMetadata = info.getServerMetadata();
				if (serverMetadata != null) {
					TransportMessage transport = new TransportMessage();
					transport.setServerMetadata(serverMetadata);
					sendMessage(marshaller, transport);
				}

				Round r = info.getRound();
				if (r != null) {
					TransportMessage transport = new TransportMessage();
					transport.setRound(r);

					sendMessage(marshaller, transport);
				}

				for (Alias player : info.getPlayers()) {
					TransportMessage transport = new TransportMessage();
					transport.setAlias(player);

					sendMessage(marshaller, transport);
				}

				for (Score score : info.getScores()) {
					TransportMessage transport = new TransportMessage();
					transport.setScore(score);

					sendMessage(marshaller, transport);
				}
			} catch (JAXBException e) {
				LOG.error(e.getMessage(), e);
			}

		}

		/**
		 * This function sends a message to ActiveMQ.
		 *
		 * @param marshaller
		 *            The marshaller to use.
		 * @param transportMessage
		 *            The message to send.
		 */
		private void sendMessage(Marshaller marshaller,
				TransportMessage transportMessage) {
			try {
				StringWriter writer = new StringWriter();
				marshaller.marshal(transportMessage, writer);

				producer.send(session.createTextMessage(writer.toString()));
			} catch (JMSException | JAXBException e) {
				LOG.error(e.getMessage(), e);
			}
		}

	}

	/**
	 * The class logger.
	 */
	private static final Logger LOG = LoggerFactory
			.getLogger(BF1942Scraper.class);

	/**
	 * The default query port.
	 */
	public static final int DEFAULT_QUERY_PORT = 22000;

	/**
	 * The JAXB context.
	 */
	private final JAXBContext jaxbContext;

	/**
	 * The status of the scraper.
	 */
	private ScraperStatus status = ScraperStatus.NotRunning;

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
	private Connection conn;

	/**
	 * The ActiveMQ session.
	 */
	private Session session;

	/**
	 * The ActiveMQ topic.
	 */
	private Topic eventTopic;

	/**
	 * This constructor sets the log directory, the host & the query port.
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
	public BF1942Scraper(@Nonnull String activeMqConnection,
			@Nonnull String logDirectory, @Nonnull String host,
			@Nonnegative int queryPort) throws JMSException, JAXBException {
		this.logDirectory = logDirectory;
		this.host = host;
		this.queryPort = queryPort;

		jaxbContext = JAXBContext.newInstance(TransportMessage.class);

		ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(
				activeMqConnection);
		conn = cf.createConnection();
		conn.start();
	}

	/**
	 * This constructor sets the log directory and host (query port is default).
	 *
	 * @param activeMqConnection
	 *            The connection string to use for ActiveMQ.
	 * @param logDirectory
	 *            The directory containing the log files.
	 * @param host
	 *            The hostname of the server.
	 * @throws JMSException
	 *             When a problem occurs connecting to ActiveMQ.
	 * @throws JAXBException
	 *             When a problem setting up the JAXB Context.
	 */
	public BF1942Scraper(@Nonnull String activeMqConnection,
			@Nonnull String logDirectory, @Nonnull String host)
			throws JMSException, JAXBException {
		this(activeMqConnection, logDirectory, host, DEFAULT_QUERY_PORT);
	}

	@Override
	public ScraperStatus getStatus() {
		return status;
	}

	@Override
	public synchronized void start() {
		if (thread == null) {
			status = ScraperStatus.Initializing;
			try {
				LOG.info("Starting query client on {}:{}", new Object[] { host,
						queryPort });
				queryClient = new GamespyQueryClient(host, queryPort);
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
				status = ScraperStatus.InError;
				return;
			}
			try {
				session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
				eventTopic = session.createTopic(DaoConstant.EVENT_TOPIC);

				BF1942InfoQueryListener queryListener = new BF1942InfoQueryListener();
				queryClient.registerListener("info", queryListener);
				queryClient.registerListener("players", queryListener);
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
		status = ScraperStatus.AllData;

		File dir = new File(logDirectory);

		File lastLog = null;

		while (thread == Thread.currentThread()) {
			TreeSet<File> logFiles = new TreeSet<File>(Arrays.asList(dir
					.listFiles(new FilenameFilter() {
						@Override
						public boolean accept(File file, String s) {
							return s.startsWith("ev_") && s.endsWith(".xml");
						}
					})));

			if (!logFiles.isEmpty()) {
				File logFile = logFiles.last();
				if (!logFile.equals(lastLog)) {
					lastLog = logFile;

					LOG.info("Opening log file {}", new Object[] { logFile.getAbsolutePath() });

					try (InputStream is = new FileInputStream(logFile)) {
						LogStreamer streamer = new LogStreamer(is, session,
								session.createProducer(eventTopic));
						streamer.run();
					} catch (IOException | JMSException e) {
						LOG.error(e.getMessage(), e);
					}

					LOG.info("Closing log file {}", new Object[] { logFile.getAbsolutePath() });
				} else {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						break;
					}
				}
			}
		}
	}
}
