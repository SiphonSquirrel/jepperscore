package jepperscore.scraper.etl.scraper;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.annotation.Nonnull;

import jepperscore.dao.IMessageDestination;
import jepperscore.dao.model.Alias;
import jepperscore.dao.model.Event;
import jepperscore.dao.model.EventCode;
import jepperscore.dao.model.Game;
import jepperscore.dao.model.Round;
import jepperscore.dao.model.Team;
import jepperscore.dao.transport.TransportMessage;
import jepperscore.scraper.common.GameManager;
import jepperscore.scraper.common.PlayerManager;
import jepperscore.scraper.common.RoundManager;
import jepperscore.scraper.common.logparser.AbstractLineLogParser;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This log parser watches the multiplayer log file for relevant events.
 *
 * @author Chuck
 *
 */
public class ETLLogParser extends AbstractLineLogParser {

	/**
	 * Log string for client disconnection.
	 */
	private static final String LOG_STRING_CLIENT_DISCONNECT = "ClientDisconnect: ";

	/**
	 * Log string for init game event.
	 */
	private static final String LOG_STRING_INIT_GAME = "InitGame: ";

	/**
	 * Log string for shutdonw game event.
	 */
	private static final String LOG_STRING_SHUTDOWN_GAME = "ShutdownGame:";

	/**
	 * Log string for kill event.
	 */
	private static final String LOG_STRING_KILL = "Kill: ";

	/**
	 * Log string for user info changing.
	 */
	private static final String LOG_STRING_CLIENT_USERINFO_CHANGED = "ClientUserinfoChanged: ";

	/**
	 * The logger for the parser.
	 */
	private Logger LOG = LoggerFactory.getLogger(ETLLogParser.class);

	/**
	 * The game name.
	 */
	private static final String GAME_NAME = "ET-Legacy";

	/**
	 * The player manager.
	 */
	private PlayerManager playerManager;

	/**
	 * The message destination.
	 */
	private IMessageDestination messageDestination;

	/**
	 * The round manager.
	 */
	private RoundManager roundManager;

	/**
	 * The game manager.
	 */
	private GameManager gameManager;

	/**
	 * This constructor parses log entries from a stream.
	 *
	 * @param stream
	 *            The stream to read.
	 * @param messageDestination
	 *            The {@link IMessageDestination} to use.
	 * @param playerManager
	 *            The player manager to use.
	 * @param roundManager
	 *            The {@link RoundManager} to use.
	 * @param gameManager
	 *            The {@link GameManager} to use.
	 */
	public ETLLogParser(@Nonnull InputStream stream,
			@Nonnull IMessageDestination messageDestination,
			@Nonnull PlayerManager playerManager,
			@Nonnull RoundManager roundManager, @Nonnull GameManager gameManager) {
		super(stream, StandardCharsets.UTF_8, false);
		this.messageDestination = messageDestination;
		this.playerManager = playerManager;
		this.roundManager = roundManager;
		this.gameManager = gameManager;
	}

	@Override
	protected void handleNewLine(String line) {
		if (line.startsWith(LOG_STRING_INIT_GAME)) {
			// InitGame:
			// \g_maxlivesRespawnPenalty\0\voteFlags\4352\g_balancedteams\1\g_maxGameClients\0\g_covertopsChargeTime\30000\g_soldierChargeTime\20000\g_LTChargeTime\40000\g_engineerChargeTime\30000\g_medicChargeTime\45000\g_bluelimbotime\20000\g_redlimbotime\30000\gamename\Bobot
			// 5.00\g_axismaxlives\0\g_alliedmaxlives\0\g_heavyWeaponRestriction\100\g_gametype\2\g_antilag\1\g_voteFlags\0\g_minGameClients\8\g_needpass\0\g_maxlives\0\g_friendlyFire\1\sv_allowAnonymous\0\sv_floodProtect\1\sv_maxPing\0\sv_minPing\0\sv_maxRate\25000\sv_maxclients\24\sv_hostname\ET
			// Legacy
			// Host\sv_privateClients\4\mapname\oasis\protocol\84\timelimit\30\version\ET
			// 2.60b win-x86 May 8 2006
			Round oldRound = roundManager.getCurrentRound();
			if (oldRound != null) {
				oldRound.setEnd(new DateTime());
				roundManager.provideRoundRecord(oldRound);
			}

			String data = line.substring(LOG_STRING_INIT_GAME.length()).trim();

			Game game = new Game();
			game.setName(GAME_NAME);

			gameManager.provideGameRecord(game);

			roundManager.newRound();

			Round round = roundManager.getCurrentRound();
			round.setGame(game);
			round.setId(UUID.randomUUID().toString());
			round.setStart(new DateTime());

			String[] arr = data.substring(LOG_STRING_INIT_GAME.length()).trim()
					.split("\\\\");
			for (int i = 1; i < (arr.length - 1); i += 2) {
				String key = arr[i];
				String value = arr[i + 1];

				switch (key) {
				case "mapname": {
					round.setMap(value);
					break;
				}
				default:
					break;
				}
			}

			roundManager.provideRoundRecord(round);
		} else if (line.startsWith(LOG_STRING_SHUTDOWN_GAME)) {
			// ShutdownGame:
			Round round = roundManager.getCurrentRound();
			if (round == null) {
				LOG.warn("Round was null during shutdown!");
			} else {
				round.setEnd(new DateTime());
				roundManager.provideRoundRecord(round);
			}
		} else if (line.startsWith(LOG_STRING_CLIENT_USERINFO_CHANGED)) {
			// ClientUserinfoChanged:
			// 0
			// n\Rukus\t\3\c\0\r\0\m\0000000\s\0000000\dn\\dr\0\w\0\lw\0\sw\0\mu\0\ref\0
			String data = line.substring(
					LOG_STRING_CLIENT_USERINFO_CHANGED.length()).trim();
			int idSpace = data.indexOf(" ");

			Alias alias = new Alias();
			alias.setDecorationStyle(Alias.DECORATION_STYLE_QUAKE3);

			String id = data.substring(0, idSpace);
			alias.setId(id);

			String[] arr = data.substring(idSpace + 1).split("\\\\");
			for (int i = 0; i < (arr.length - 1); i += 2) {
				String key = arr[i];
				String value = arr[i + 1];

				switch (key) {
				case "n": {
					alias.setName(value);
					break;
				}
				case "t": {
					if ("1".equals(value)) {
						Team t = new Team(ETLConstants.TEAM_AXIS);
						alias.setTeam(t);
					} else if ("2".equals(value)) {
						Team t = new Team(ETLConstants.TEAM_ALLIES);
						alias.setTeam(t);
					}
					break;
				}
				default:
					break;
				}
			}

			playerManager.providePlayerRecord(alias);
		} else if (line.startsWith(LOG_STRING_CLIENT_DISCONNECT)) {
			String id = line.substring(LOG_STRING_CLIENT_DISCONNECT.length()).trim();
			Alias alias = playerManager.getPlayer(id);
			alias.setPresent(false);
			playerManager.providePlayerRecord(alias);
		} else if (line.startsWith(LOG_STRING_KILL)) {

			// Kill: 1 1 64: Eva killed Eva by MOD_SWITCHTEAM
			String data = line.substring(LOG_STRING_KILL.length()).trim();
			String[] arr = data.split(" ");

			String attackerId = arr[0];
			String victimId = arr[1];
			String weapon = arr[arr.length - 1];

			Alias victim = playerManager.getPlayer(victimId);
			Alias attacker = playerManager.getPlayer(attackerId);
			victim.setDecorationStyle(Alias.DECORATION_STYLE_QUAKE3);
			attacker.setDecorationStyle(Alias.DECORATION_STYLE_QUAKE3);

			if (victim.getName().isEmpty()) {
				victim.setName(arr[5]);
				playerManager.providePlayerRecord(victim);
			}

			if (attacker.getName().isEmpty()) {
				attacker.setName(arr[3]);
				playerManager.providePlayerRecord(attacker);
			}

			Event e = new Event();
			e.setVictim(victim);
			e.setAttacker(attacker);
			Round round = roundManager.getCurrentRound();
			if (round != null) {
				e.setRound(round);
			}

			EventCode eventCode = new EventCode();
			e.setEventCode(eventCode);

			String prefix = "";
			if (attackerId.equals(victimId)) {
				prefix = "TK:";
				eventCode.setCode("teamkill");
			} else {
				eventCode.setCode(EventCode.EVENT_CODE_KILL);
			}

			e.setEventCode(eventCode);

			e.setEventText(String.format("{attacker} [%s%s] {victim}", prefix,
					weapon));

			TransportMessage transportMessage = new TransportMessage();
			transportMessage.setEvent(e);
			if (round != null) {
				transportMessage.setSessionId(round.getId());
			} else {
				LOG.warn("Round was null during event!");
			}
			messageDestination.sendMessage(transportMessage);
		}
	}

}
