package jepperscore.scraper.bf1942.scraper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import jepperscore.dao.IMessageDestination;
import jepperscore.dao.model.Alias;
import jepperscore.dao.model.Game;
import jepperscore.dao.model.Round;
import jepperscore.dao.model.Score;
import jepperscore.dao.model.ServerMetadata;
import jepperscore.dao.model.Team;
import jepperscore.dao.transport.TransportMessage;
import jepperscore.scraper.common.Scraper;
import jepperscore.scraper.common.ScraperStatus;
import jepperscore.scraper.common.SimpleDataManager;
import jepperscore.scraper.common.query.QueryCallbackInfo;
import jepperscore.scraper.common.query.QueryClientListener;
import jepperscore.scraper.common.query.gamespy.GamespyMessageSplitter;
import jepperscore.scraper.common.query.gamespy.GamespyQueryCallbackInfo;
import jepperscore.scraper.common.query.gamespy.GamespyQueryClient;
import jepperscore.scraper.common.query.gamespy.GamespyQueryUtil;
import jepperscore.scraper.common.rcon.RconClient;

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
	private class BF1942QueryListener implements QueryClientListener,
			GamespyMessageSplitter {

		/**
		 * Default constructor.
		 */
		public BF1942QueryListener() {
		}

		@Override
		public void queryClient(QueryCallbackInfo info) {
			ServerMetadata serverMetadata = info.getServerMetadata();
			if (serverMetadata != null) {
				TransportMessage transport = new TransportMessage();
				Round round = dataManager.getCurrentRound();
				if (round != null) {
					transport.setSessionId(round.getId());
				}
				transport.setServerMetadata(serverMetadata);
				messageDestination.sendMessage(transport);

				Map<String, String> metadata = serverMetadata.getMetadata();
				if (metadata != null) {
					String gametype = metadata.get("gametype");
					String tickets1Str = metadata.get("tickets1");
					String tickets2Str = metadata.get("tickets2");
					String map = metadata.get("mapname");

					Game game = new Game();
					game.setName(BF1942Constants.GAME_NAME);
					game.setMod("");
					game.setGametype(gametype);

					dataManager.provideGameRecord(game);
					if (round != null) {
						round.setMap(map);
						dataManager.provideRoundRecord(round);
					}

					if (tickets1Str != null) {
						try {
							float tickets = Float.parseFloat(tickets1Str);

							Team team = new Team();
							team.setScore(tickets);

							dataManager.provideTeamRecord("1", team);
						} catch (NumberFormatException e) {
							// Do nothing.
						}
					}

					if (tickets2Str != null) {
						try {
							float tickets = Float.parseFloat(tickets2Str);

							Team team = new Team();
							team.setScore(tickets);

							dataManager.provideTeamRecord("2", team);
						} catch (NumberFormatException e) {
							// Do nothing.
						}
					}

				}
			}

			Round r = info.getRound();
			if (r != null) {
				dataManager.provideRoundRecord(r);
			}

			for (Alias player : info.getPlayers()) {
				dataManager.providePlayerRecord(player);
			}

			for (Score score : info.getScores()) {
				dataManager.provideScoreRecord(score);
			}
		}

		@Override
		public GamespyQueryCallbackInfo splitMessage(String queryType,
				String[] messageArray) {
			switch (queryType) {
			case "info":
				return handleInfo(messageArray);
			case "players":
				return handlePlayers(messageArray);
			default:
				LOG.error("Unsure how to handle " + queryType);
				return null;
			}
		}

		/**
		 * Handles the info callback.
		 *
		 * @param messageArray
		 *            The message.
		 * @return The callback info.
		 */
		private GamespyQueryCallbackInfo handleInfo(String[] messageArray) {
			ServerMetadata serverMetadata = new ServerMetadata();

			for (int i = 1; i < (messageArray.length - 1); i += 2) {
				String key = messageArray[i];
				String value = messageArray[i + 1];

				switch (key) {
				case "hostname":
					serverMetadata.setServerName(value);
					break;
				case "final":
				case "queryid":
					break;
				default:
					serverMetadata.getMetadata().put(key, value);
					break;
				}
			}

			GamespyQueryCallbackInfo info = new GamespyQueryCallbackInfo();
			info.setServerMetadata(serverMetadata);

			return info;
		}

		/**
		 * Handles the players callback.
		 *
		 * @param messageArray
		 *            The message.
		 * @return The callback info.
		 */
		private GamespyQueryCallbackInfo handlePlayers(String[] messageArray) {
			GamespyQueryCallbackInfo info = new GamespyQueryCallbackInfo();

			Map<String, Map<String, String>> playerInfo = GamespyQueryUtil
					.parsePlayers(messageArray, 1, new String[] { "teamname" });

			List<Score> scores = new LinkedList<Score>();

			for (Map<String, String> playerProperties : playerInfo.values()) {
				String name = playerProperties.get("playername");
				String scoreStr = playerProperties.get("score");
				if ((name != null) && (scoreStr != null)) {
					try {
						float scoreValue = Float.parseFloat(scoreStr);

						Alias player = dataManager.getPlayerByName(name);
						if (player != null) {
							Score score = new Score();
							score.setAlias(player);
							score.setScore(scoreValue);

							scores.add(score);
						}
					} catch (NumberFormatException e) {
					}
				}
			}

			for (int i = 1; i < messageArray.length; i++) {
				switch (messageArray[i]) {
				case "teamname_0": {
					Team team = dataManager.getTeamById("1");
					if (team == null) {
						team = new Team();
					}
					team.setTeamName(messageArray[i + 1]);

					dataManager.provideTeamRecord("1", team);
				}
				break;
				
				case "teamname_1": {
					Team team = dataManager.getTeamById("2");
					if (team == null) {
						team = new Team();
					}
					team.setTeamName(messageArray[i + 1]);

					dataManager.provideTeamRecord("2", team);
				}
				break;
				
				default:
					break;
				}
			}

			info.setScores(scores);

			return info;
		}
	}

	/**
	 * The class logger.
	 */
	private static final Logger LOG = LoggerFactory
			.getLogger(BF1942Scraper.class);

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
	private GamespyQueryClient queryClient;

	/**
	 * The message destination.
	 */
	private final IMessageDestination messageDestination;

	/**
	 * The player manager to use.
	 */
	private SimpleDataManager dataManager;

	/**
	 * The RCON client to use.
	 */
	private RconClient rconClient;

	/**
	 * This constructor sets up to BF1942 scraper.
	 *
	 * @param messageDestination
	 *            The message destination to use.
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
	 */
	public BF1942Scraper(@Nonnull IMessageDestination messageDestination,
			@Nonnull String modDirectory, @Nonnull String host,
			@Nonnegative int queryPort, @Nonnegative int rconPort,
			@Nonnull String rconUser, @Nonnull String rconPassword) {
		this.logDirectory = modDirectory + "/Logs";
		this.host = host;
		this.queryPort = queryPort;
		this.messageDestination = messageDestination;

		rconClient = new BF1942RconClient(host, rconPort, rconUser,
				rconPassword);

		dataManager = new SimpleDataManager(messageDestination);
	}

	@Override
	public ScraperStatus getStatus() {
		return status;
	}

	@Override
	public synchronized void start() {
		if (thread == null) {
			status = ScraperStatus.Initializing;

			dataManager.newRound();

			if (!new File(logDirectory).exists()) {
				LOG.error("Log directory does not exist.");
				status = ScraperStatus.InError;
				return;
			}

			try {
				LOG.info("Starting query client on {}:{}", new Object[] { host,
						queryPort });
				queryClient = new GamespyQueryClient(host, queryPort);
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
				status = ScraperStatus.InError;
				return;
			}

			BF1942QueryListener queryListener = new BF1942QueryListener();

			queryClient.registerMessageSplitter("info", queryListener);
			queryClient.registerListener("info", queryListener);

			queryClient.registerMessageSplitter("players", queryListener);
			queryClient.registerListener("players", queryListener);

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
								BF1942LogStreamer streamer = new BF1942LogStreamer(
										is, messageDestination,
										dataManager, dataManager, dataManager, dataManager);

								logThread = new Thread(streamer);
								logThread.setDaemon(true);
								logThread.start();
							} catch (IOException e) {
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

						dataManager.providePlayerRecord(player);
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
