package jepperscore.scrapers.ut2004;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This scraper works with UT2004 + mods.
 *
 * @author Chuck
 *
 */
public class UT2004Scraper implements Scraper, Runnable {

	/**
	 * This is the listener for info query.
	 *
	 * @author Chuck
	 *
	 */
	private class UT2004QueryListener implements QueryClientListener,
			GamespyMessageSplitter {

		/**
		 * Default constructor.
		 */
		public UT2004QueryListener() {

		}

		@Override
		public void queryClient(QueryCallbackInfo info) {
			ServerMetadata serverMetadata = info.getServerMetadata();
			if (serverMetadata != null) {
				TransportMessage transport = new TransportMessage();
				transport.setServerMetadata(serverMetadata);
				messageDestination.sendMessage(transport);

				Map<String, String> metadata = serverMetadata.getMetadata();
				if (metadata != null) {
					String gametype = metadata.get("gametype");
					String map = metadata.get("mapname");

					Game game = new Game();
					game.setName(UT2004Constants.GAME_NAME);
					game.setMod("");
					game.setGametype(gametype);

					dataManager.provideGameRecord(game);
					Round round = dataManager.getCurrentRound();
					if (round != null) {
						round.setMap(map);
						dataManager.provideRoundRecord(round);
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
			case "status":
				return handleStatus(messageArray);
			case "bots":
				return handleBots(messageArray);
			case "teams":
				return handleTeams(messageArray);
			default:
				LOG.error("Unsure how to handle " + queryType);
				return null;
			}
		}

		/**
		 * This handles status message splitting.
		 *
		 * @param messageArray
		 *            The split message to convert.
		 * @return The converted message.
		 */
		private GamespyQueryCallbackInfo handleStatus(String[] messageArray) {
			GamespyQueryCallbackInfo info = new GamespyQueryCallbackInfo();
			ServerMetadata serverMetadata = new ServerMetadata();
			info.setServerMetadata(serverMetadata);

			for (int i = 1; i < (messageArray.length - 1); i += 2) {
				String key = messageArray[i];
				String value = messageArray[i + 1];

				if (key.startsWith("player_")) {
					parseScores(info, messageArray, i, "player");
					break;
				}

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

			return info;
		}

		/**
		 * This handles bots message splitting.
		 *
		 * @param messageArray
		 *            The split message to convert.
		 * @return The converted message.
		 */
		private GamespyQueryCallbackInfo handleBots(String[] messageArray) {
			GamespyQueryCallbackInfo info = new GamespyQueryCallbackInfo();
			parseScores(info, messageArray, 1, "bot");
			return info;
		}

		/**
		 * This function parses the players.
		 *
		 * @param info
		 *            The info to update.
		 * @param messageArray
		 *            The message array to parse.
		 * @param startIndex
		 *            the index to start from.
		 * @param nameField
		 *            The field to use for a player's name.
		 */
		private void parseScores(GamespyQueryCallbackInfo info,
				String[] messageArray, int startIndex, String nameField) {

			Map<String, Map<String, String>> playerInfo = GamespyQueryUtil
					.parsePlayers(messageArray, startIndex);

			List<Score> scores = new LinkedList<Score>();
			List<Alias> players = new LinkedList<Alias>();

			for (Map<String, String> playerProperties : playerInfo.values()) {
				String name = playerProperties.get(nameField);
				String scoreStr = playerProperties.get("frags");
				String team = playerProperties.get("team");

				if ((name != null) && (scoreStr != null) && (team != null)) {
					try {
						float scoreValue = Float.parseFloat(scoreStr);

						Alias player = dataManager.getPlayerByName(name);
						if (player != null) {
							player.setTeam(dataManager.getTeamById(team));
							players.add(player);

							Score score = new Score();
							score.setAlias(player);
							score.setScore(scoreValue);
							scores.add(score);
						}
					} catch (NumberFormatException e) {
					}
				}
			}

			info.setScores(scores);
			info.setPlayers(players);
		}

		/**
		 * This handles teams message splitting.
		 *
		 * @param messageArray
		 *            The split message to convert.
		 * @return The converted message.
		 */
		private GamespyQueryCallbackInfo handleTeams(String[] messageArray) {
			GamespyQueryCallbackInfo info = new GamespyQueryCallbackInfo();

			Map<String, Map<String, String>> teamInfo = GamespyQueryUtil
					.parsePlayers(messageArray, 1);

			for (Entry<String, Map<String, String>> teamEntry : teamInfo
					.entrySet()) {
				Map<String, String> teamProperties = teamEntry.getValue();
				String teamName = teamProperties.get("team");
				String scoreStr = teamProperties.get("score");

				try {
					float score = Float.parseFloat(scoreStr);

					Team team = new Team();
					team.setTeamName(teamName);
					team.setScore(score);

					dataManager.provideTeamRecord(teamEntry.getKey(), team);
				} catch (NumberFormatException e) {
					// Do nothing
				}
			}

			return info;
		}

	}

	/**
	 * The class logger.
	 */
	private static final Logger LOG = LoggerFactory
			.getLogger(UT2004Scraper.class);

	/**
	 * The status of the scraper.
	 */
	private volatile ScraperStatus status = ScraperStatus.NotRunning;

	/**
	 * The log directory to watch.
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
	private GamespyQueryClient queryClient;

	/**
	 * The player manager to use.
	 */
	private SimpleDataManager dataManager;

	/**
	 * The message destination to use.
	 */
	private IMessageDestination messageDestination;

	/**
	 * This constructor sets the ETQW scraper.
	 *
	 * @param messageDestination
	 *            The message destination to use.
	 * @param logFile
	 *            The directory containing the log files.
	 * @param host
	 *            The hostname of the server.
	 * @param queryPort
	 *            The query port of the server.
	 */
	public UT2004Scraper(@Nonnull IMessageDestination messageDestination,
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
			dataManager.setWipePlayersOnNewRound(false);

			try {
				LOG.info("Starting query client on {}:{}", new Object[] { host,
						queryPort });
				queryClient = new GamespyQueryClient(host, queryPort);
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
				status = ScraperStatus.InError;
				return;
			}

			UT2004QueryListener queryListener;
			queryListener = new UT2004QueryListener();
			queryClient.registerMessageSplitter("status", queryListener);
			queryClient.registerListener("status", queryListener);

			queryClient.registerMessageSplitter("bots", queryListener);
			queryClient.registerListener("bots", queryListener);

			queryClient.registerMessageSplitter("teams", queryListener);
			queryClient.registerListener("teams", queryListener);

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
			UT2004LogParser parser = new UT2004LogParser(is, messageDestination, dataManager, dataManager);
			parser.run();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}
}
