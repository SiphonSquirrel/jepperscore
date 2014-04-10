package jepperscore.scraper.callofduty.scraper;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

import org.apache.commons.codec.digest.DigestUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This log parser watches the multiplayer log file for relevant events.
 *
 * @author Chuck
 *
 */
public class Cod4LogParser extends AbstractLineLogParser {

	/**
	 * The logger for the parser.
	 */
	private Logger LOG = LoggerFactory.getLogger(Cod4LogParser.class);

	/**
	 * A separating line seen in the log file.
	 */
	private static final String SEP_LINE = "------------------------------------------------------------";

	/**
	 * The game name.
	 */
	private static final String GAME_NAME = "COD4";

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
	 * The game.
	 */
	private Game game;

	/**
	 * The round.
	 */
	private Round round;

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
	public Cod4LogParser(@Nonnull InputStream stream,
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
		if (!line.endsWith(SEP_LINE)) {
			int sepPosition = line.indexOf(';');
			if (sepPosition < 0) {
				sepPosition = Math.max(line.indexOf(": "),
						line.lastIndexOf(":"));
				int spacePosition = line.lastIndexOf(' ', sepPosition);
				if (sepPosition >= 0) {
					String state = line.substring(spacePosition + 1,
							sepPosition).trim();
					String data = line.substring(sepPosition + 1).trim();

					handleStateChange(state, data);
				} else {
					LOG.warn("Not sure how to parse: " + line);
				}
				return;
			} else {
				int spacePosition = line.lastIndexOf(' ', sepPosition);

				String event = line.substring(spacePosition + 1, sepPosition);
				String parsableLine = line.substring(sepPosition + 1);

				String[] lineArr = parsableLine.split(";");
				handleEvent(event, lineArr);
			}
		}
	}

	/**
	 * This function handles a change in state.
	 *
	 * @param state
	 *            The state we changed to.
	 * @param data
	 *            Any associated data.
	 */
	private void handleStateChange(String state, String data) {
		switch (state) {
		case "InitGame": { // InitGame:
							// \g_compassShowEnemies\0\g_gametype\dm\gamename\Call
							// of Duty
							// 4\mapname\mp_backlot\protocol\2\shortversion\1.3\sv_allowAnonymous\0\sv_disableClientConsole\0\sv_floodprotect\1\sv_hostname\Bozo\sv_maxclients\24\sv_maxPing\0\sv_maxRate\5000\sv_minPing\0\sv_privateClients\0\sv_punkbuster\0\sv_pure\1\sv_voice\0\
							// ui_maxclients\32
			LOG.info("Game started");

			String[] dataArr = data.substring(1).split("\\\\");
			Map<String, String> dataMap = new HashMap<String, String>();
			for (int i = 0; i < (dataArr.length - 1); i += 2) {
				dataMap.put(dataArr[i], dataArr[i + 1]);
			}

			String id = DigestUtils.md5Hex(state) + getLineNumber();

			if (round != null) {
				round.setEnd(new DateTime());
				roundManager.provideRoundRecord(round);
			}

			game = new Game(GAME_NAME, dataMap.get("g_gametype"), "");
			gameManager.provideGameRecord(game);

			round = new Round(id, new DateTime(), null, game,
					dataMap.get("mapname"));

			playerManager.newRound(round);

			break;
		}
		case "ShutdownGame": { // ShutdownGame:
			LOG.info("Game ended");
			break;
		}
		case "ExitLevel": { // ExitLevel: executed
			break;
		}
		default: {
			LOG.warn("Unknown state: " + state);
			break;
		}
		}
	}

	/**
	 * This function handles an event.
	 *
	 * @param event
	 *            The event.
	 * @param eventArray
	 *            The event data, previously semicolon seperated.
	 */
	private void handleEvent(String event, String[] eventArray) {
		switch (event) {
		case "J": // Join: J;00000000000000000000000000000000;1;Mimius
		case "Q": { // Quit: Q;00000000000000000000000000000000;3;Jsp
			if (eventArray.length != 3) {
				LOG.warn("Unrecognized join data: "
						+ Arrays.toString(eventArray));
				break;
			}
			String playerId = eventArray[1];
			String playerName = eventArray[2];

			Alias alias = new Alias();
			alias.setId(playerId);
			alias.setName(playerName);
			alias.setPresent("J".equals(eventArray));
			if (game != null) {
				alias.setGame(game);
			}
			playerManager.providePlayerRecord(alias);
			break;
		}
		case "K": // Kill:
					// K;00000000000000000000000000000000;1;;Mimius;00000000000000000000000000000000;2;;JMLX;m16_gl_mp;55;MOD_HEAD_SHOT;head
		case "D": { // Damage:
					// D;00000000000000000000000000000000;2;axis;JMLX;00000000000000000000000000000000;1;allies;Mimius;uzi_reflex_mp;29;MOD_PISTOL_BULLET;left_arm_upper
			if (eventArray.length != 12) {
				LOG.warn("Unrecognized damage data: "
						+ Arrays.toString(eventArray));
				break;
			}

			String victimPlayerId = eventArray[1];
			String victimPlayerTeam = eventArray[2];
			String victimPlayerName = eventArray[3];

			Alias victim = playerManager.getPlayer(victimPlayerId);
			if (victim == null) {
				victim = new Alias();
				victim.setId(victimPlayerId);
			}
			victim.setName(victimPlayerName);
			if (game != null) {
				victim.setGame(game);
			}
			if ((game != null) && ("dm".equals(game.getGametype()))
					&& (!victimPlayerTeam.isEmpty())) {
				victim.setTeam(new Team(victimPlayerTeam));
			}

			playerManager.providePlayerRecord(victim);

			String attackerPlayerId = eventArray[5];
			String attackerPlayerTeam = eventArray[6];
			String attackerPlayerName = eventArray[7];

			Alias attacker;
			if ("-1".equals(attackerPlayerId)) {
				attacker = victim;
			} else {
				attacker = playerManager.getPlayer(attackerPlayerId);
				if (attacker == null) {
					attacker = new Alias();
					attacker.setId(attackerPlayerId);
				}
				attacker.setName(attackerPlayerName);
				if (game != null) {
					attacker.setGame(game);
				}
				if ((game != null) && ("dm".equals(game.getGametype()))
						&& (!attackerPlayerTeam.isEmpty())) {
					attacker.setTeam(new Team(attackerPlayerTeam));
				}

				playerManager.providePlayerRecord(attacker);
			}

			if ("K".equals(event)) {
				String attackerWeapon = eventArray[8];
				String damageType = eventArray[10];
				String damageLocation = eventArray[11];

				Event e = new Event();
				e.setVictim(victim);
				e.setAttacker(attacker);
				e.setRound(round);

				EventCode eventCode = new EventCode();
				e.setEventCode(eventCode);
				eventCode.setExtra("type:" + damageType + ",location:"
						+ damageLocation);

				String prefix = "";
				if (attackerPlayerId.equals(victimPlayerId)) {
					prefix = "TK:";
					eventCode.setCode("teamkill");
				} else {
					eventCode.setCode(EventCode.EVENT_CODE_KILL);
				}

				e.setEventCode(eventCode);

				e.setEventText(String.format("{attacker} [%s%s] {victim}",
						prefix, attackerWeapon));

				TransportMessage transportMessage = new TransportMessage();
				transportMessage.setEvent(e);
				Round round = roundManager.getCurrentRound();
				if (round != null) {
					transportMessage.setSessionId(round.getId());
				}
				messageDestination.sendMessage(transportMessage);
			}
			break;
		}
		case "say": { // Chat:
						// say;00000000000000000000000000000000;2;JMLX;TRTRDWR
			break;
		}
		case "Weapon": { // Weapon Switch?: Weapon;;0;Phantom;m16_gl_mp
			break;
		}
		default: {
			LOG.warn("Unknown event type: " + event);
			break;
		}
		}
	}
}
