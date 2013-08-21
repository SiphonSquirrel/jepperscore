package jepperscore.scraper.bf1942;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeSet;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import jepperscore.dao.DaoConstant;
import jepperscore.dao.model.Alias;
import jepperscore.dao.model.Game;
import jepperscore.dao.model.Round;
import jepperscore.dao.model.Score;
import jepperscore.dao.model.ServerMetadata;
import jepperscore.dao.model.Team;
import jepperscore.dao.transport.TransportMessage;
import jepperscore.scraper.common.MessageUtil;
import jepperscore.scraper.common.PlayerManager;
import jepperscore.scraper.common.Scraper;
import jepperscore.scraper.common.ScraperStatus;
import jepperscore.scraper.common.query.QueryCallbackInfo;
import jepperscore.scraper.common.query.QueryClient;
import jepperscore.scraper.common.query.QueryClientListener;
import jepperscore.scraper.common.query.gamespy.GamespyQueryClient;
import jepperscore.scraper.common.rcon.RconClient;

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
			ServerMetadata serverMetadata = info.getServerMetadata();
			if (serverMetadata != null) {
				TransportMessage transport = new TransportMessage();
				transport.setServerMetadata(serverMetadata);
				MessageUtil.sendMessage(producer, session, transport);

				Map<String, String> metadata = serverMetadata.getMetadata();
				if (metadata != null) {
					String gametype = metadata.get("gametype");
					String axisTicketsStr = metadata.get("tickets1");
					String alliedTicketsStr = metadata.get("tickets2");
					String map = metadata.get("mapname");

					Game game = new Game();
					game.setName(BF1942Constants.GAME_NAME);
					game.setMod("");
					game.setGametype(gametype);

					Round round = new Round();
					round.setGame(game);
					round.setMap(map);

					TransportMessage roundTransport = new TransportMessage();
					roundTransport.setRound(round);

					MessageUtil.sendMessage(producer, session, roundTransport);

					if (axisTicketsStr != null) {
						try {
							float axisTickets = Float
									.parseFloat(axisTicketsStr);

							Team axisTeam = new Team();
							axisTeam.setTeamName(BF1942Constants.AXIS_TEAM);
							axisTeam.setScore(axisTickets);

							TransportMessage ticketsTransport = new TransportMessage();
							ticketsTransport.setTeam(axisTeam);

							MessageUtil.sendMessage(producer, session,
									ticketsTransport);
						} catch (NumberFormatException e) {
							// Do nothing.
						}
					}

					if (alliedTicketsStr != null) {
						try {
							float alliedTickets = Float
									.parseFloat(alliedTicketsStr);

							Team alliedTeam = new Team();
							alliedTeam.setTeamName(BF1942Constants.ALLIED_TEAM);
							alliedTeam.setScore(alliedTickets);

							TransportMessage ticketsTransport = new TransportMessage();
							ticketsTransport.setTeam(alliedTeam);

							MessageUtil.sendMessage(producer, session,
									ticketsTransport);
						} catch (NumberFormatException e) {
							// Do nothing.
						}
					}

				}
			}

			Round r = info.getRound();
			if (r != null) {
				TransportMessage transport = new TransportMessage();
				transport.setRound(r);

				MessageUtil.sendMessage(producer, session, transport);
			}

			for (Alias player : info.getPlayers()) {
				TransportMessage transport = new TransportMessage();
				transport.setAlias(player);

				MessageUtil.sendMessage(producer, session, transport);
			}

			for (Score score : info.getScores()) {
				TransportMessage transport = new TransportMessage();
				transport.setScore(score);

				MessageUtil.sendMessage(producer, session, transport);
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
	 * The default RCON port.
	 */
	public static final int DEFAULT_RCON_PORT = 4711;

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
	 * The RCON client to use.
	 */
	private RconClient rconClient;

	/**
	 * This constructor sets up to BF1942 scraper.
	 *
	 * @param activeMqConnection
	 *            The connection string to use for ActiveMQ.
	 * @param modDirectory
	 *            The directory containing the log files.
	 * @param host
	 *            The hostname of the server.
	 * @param queryPort
	 *            The query port of the server.
	 * @param rconPort
	 *            The RCON port of the server.
	 * @param rconUser
	 *            The RCON username to login with.
	 * @param rconPassword
	 *            The RCON password to login with.
	 * @throws JMSException
	 *             When a problem occurs connecting to ActiveMQ.
	 */
	public BF1942Scraper(@Nonnull String activeMqConnection,
			@Nonnull String modDirectory, @Nonnull String host,
			@Nonnegative int queryPort, @Nonnegative int rconPort,
			@Nonnull String rconUser, @Nonnull String rconPassword)
			throws JMSException {
		this.logDirectory = modDirectory + "/Logs";
		this.host = host;
		this.queryPort = queryPort;

		ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(
				activeMqConnection);
		conn = cf.createConnection();
		conn.start();

		session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
		eventTopic = session.createTopic(DaoConstant.EVENT_TOPIC);

		rconClient = new BF1942RconClient(host, rconPort, rconUser,
				rconPassword);
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
				queryClient = new GamespyQueryClient(host, queryPort,
						playerManager);
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
				status = ScraperStatus.InError;
				return;
			}
			try {
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
		InputStream is = null;
		Thread logThread = null;

		try {
			while (thread == Thread.currentThread()) {
				if (logThread == null) {
					TreeSet<File> logFiles = new TreeSet<File>(
							Arrays.asList(dir.listFiles(new FilenameFilter() {
								@Override
								public boolean accept(File file, String s) {
									return s.startsWith("ev_")
											&& s.endsWith(".xml");
								}
							})));

					if (!logFiles.isEmpty()) {
						File logFile = logFiles.last();
						if (!logFile.equals(lastLog)) {
							lastLog = logFile;

							LOG.info("Now watching log file {}",
									new Object[] { logFile.getAbsolutePath() });

							try {
								is = new FileInputStream(logFile);
								LogStreamer streamer = new LogStreamer(is,
										session,
										session.createProducer(eventTopic),
										playerManager);

								logThread = new Thread(streamer);
								logThread.setDaemon(true);
								logThread.start();
							} catch (IOException | JMSException e) {
								LOG.error(e.getMessage(), e);
							}
						} else {
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								break;
							}
						}
					}
				}

				if (logThread != null) {
					try {
						logThread.join(1000);
					} catch (InterruptedException e) {
						stop();
					}

					if (!logThread.isAlive()) {
						logThread = null;
					}
				}

				String[] playersResult = rconClient
						.sendCommand("game.listPlayers");
				if (playersResult == null) {
					LOG.warn("Unable to fetch player list from RCON.");
				} else if (playersResult.length != 1) {
					LOG.warn("Unexpected result from fetch player list from RCON.");
				} else {
					String[] lines = playersResult[0].split("\n");
					for (String line : lines) {
						if (!line.startsWith("Id:")) {
							LOG.warn("Did not understand RCON line: " + line);
							continue;
						}

						int pos = line.indexOf(' ');
						if (pos < 0) {
							LOG.warn("Did not understand RCON line: " + line);
							continue;
						}
						String id = line.substring(3, pos);

						int pos2 = line.indexOf(" is remote ");
						if (pos2 < 0) {
							LOG.warn("Did not understand RCON line: " + line);
							continue;
						}

						String name = line.substring(pos + 3, pos2);

						Alias player = new Alias();
						player.setId(id);
						player.setName(name);
						player.setBot(line.contains("is an AI bot"));

						playerManager.providePlayerRecord(player);
					}
				}
			}
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// Do nothing.
				}
			}
		}
	}
}
