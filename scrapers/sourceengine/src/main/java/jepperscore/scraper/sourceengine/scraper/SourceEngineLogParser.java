package jepperscore.scraper.sourceengine.scraper;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import jepperscore.dao.IMessageDestination;
import jepperscore.dao.model.Alias;
import jepperscore.dao.model.Event;
import jepperscore.dao.model.EventCode;
import jepperscore.dao.model.Round;
import jepperscore.dao.model.Team;
import jepperscore.dao.transport.TransportMessage;
import jepperscore.scraper.common.PlayerManager;
import jepperscore.scraper.common.RoundManager;
import jepperscore.scraper.common.query.gamespy.GamespyQueryClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This log parser connects to HDLS via UDP to receive log lines. See also:
 * https://developer.valvesoftware.com/wiki/HL_Log_Standard
 *
 * @author Chuck
 *
 */
public class SourceEngineLogParser implements Runnable {

	/**
	 * The message header on incoming packets.
	 */
	public static final byte[] MESSAGE_HEADER = new byte[] {(byte) 0xFF,(byte) 0xFF, (byte) 0xFF, (byte) 0xFF};

	/**
	 * The logger for this class.
	 */
	private static final Logger LOG = LoggerFactory
			.getLogger(GamespyQueryClient.class);

	/**
	 * The charset to use for decoding.
	 */
	private static final Charset CHARSET = StandardCharsets.UTF_8;

	/**
	 * The RegEx string to use.
	 */
	private static final Pattern LOG_LINE_REGEX = Pattern
			.compile("[RL]* ([0-9][0-9])/([0-9][0-9])/([0-9][0-9][0-9][0-9]) - ([0-9][0-9]):([0-9][0-9]):([0-9][0-9]): *(.*)");

	/**
	 * Player ID regex.
	 */
	private static final String PLAYER_ID_REGEX_STR = "\"([^<\"]*)<([^>\"]*)><([^>\"]*)><([^>\"]*)>\"";

	/**
	 * Kill Regex to match: "Name<uid><wonid><team>" killed
	 * "Name<uid><wonid><team>" with "weapon"
	 */
	private static final Pattern KILL_REGEX = Pattern
			.compile(PLAYER_ID_REGEX_STR + " killed " + PLAYER_ID_REGEX_STR
					+ " with \"([^\"]+)\"");

	/**
	 * The player manager.
	 */
	private PlayerManager playerManager;

	/**
	 * The round manager.
	 */
	private RoundManager roundManager;

	/**
	 * The current message destination.
	 */
	private IMessageDestination messageDestination;

	/**
	 * The thread for this parser.
	 */
	private volatile Thread thread = null;

	/**
	 * The UDP socket.
	 */
	private DatagramSocket socket;

	/**
	 * This constructor parses log entries from a stream.
	 *
	 * @param port
	 *            The UDP log port of the server.
	 * @param messageDestination
	 *            The message destination to use.
	 * @param playerManager
	 *            The player manager to use.
	 * @param roundManager
	 *            The {@link RoundManager} to use.
	 * @throws SocketException
	 *             Thrown if something goes wrong setting up the socket.
	 */
	public SourceEngineLogParser(@Nonnegative int port,
			@Nonnull IMessageDestination messageDestination,
			@Nonnull PlayerManager playerManager,
			@Nonnull RoundManager roundManager) throws SocketException {
		this.messageDestination = messageDestination;
		this.playerManager = playerManager;
		this.roundManager = roundManager;

		socket = new DatagramSocket(port);
	}

	/**
	 * Returns the port this service is bound to.
	 * @return The port.
	 */
	public int getPort() {
		return socket.getLocalPort();
	}

	/**
	 * Starts the log parser.
	 */
	public synchronized void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.setDaemon(true);
			thread.start();
		}
	}

	/**
	 * Stops the log parser.
	 */
	public synchronized void stop() {
		if (thread != null) {
			thread.interrupt();
			thread = null;
		}
	}

	@Override
	public void run() {
		while (thread != null) {
			try {
				socket.setSoTimeout(1000);

				ByteBuffer recvBuffer = ByteBuffer.allocate(1024 * 8);
				recvBuffer.order(ByteOrder.LITTLE_ENDIAN);
				DatagramPacket recvPacket = new DatagramPacket(
						recvBuffer.array(), recvBuffer.capacity());
				socket.receive(recvPacket);

				byte[] data = recvPacket.getData();
				if ((recvPacket.getLength() > 4) && (data[0] == (byte) 0xFF)
						&& (data[1] == (byte) 0xFF) && (data[2] == (byte) 0xFF)
						&& (data[3] == (byte) 0xFF)) {

					handleNewLine(new String(recvPacket.getData(), 4,
							recvPacket.getLength() - 4, CHARSET));
				}
			} catch (java.net.SocketTimeoutException e) {
				// Do nothing!
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
				break;
			}
		}
	}

	/**
	 * This function creates an alias from a {@link Matcher} result.
	 * @param m The matcher to use.
	 * @param startIndex The group index to start at.
	 * @return The created {@link Alias}s.
	 */
	protected Alias createAliasFromRegEx(Matcher m, int startIndex) {
		Alias player = new Alias();

		String name = m.group(startIndex + 0);
		String id = m.group(startIndex + 1);
		String globalId = m.group(startIndex + 2);
		String team = m.group(startIndex + 3);

		player.setName(name);
		player.setId(id);
		player.setTeam(new Team(team));
		player.setBot("BOT".equals(globalId));

		return playerManager.providePlayerRecord(player);
	}

	/**
	 * This function handles new lines of logging as they come in.
	 *
	 * @param line
	 *            The line to handle.
	 */
	protected void handleNewLine(String line) {
		System.out.println("---");
		System.out.println(line);

		Matcher matcher = LOG_LINE_REGEX.matcher(line.trim());
		if (!matcher.find()) {
			return;
		}

		String data = matcher.group(7);
		System.out.println(data);

		Matcher killRegex = KILL_REGEX.matcher(data);
		if (killRegex.find()) {

			Alias victim = createAliasFromRegEx(killRegex, 1);
			Alias attacker = createAliasFromRegEx(killRegex, 5);
			String weapon = killRegex.group(9);

			Event newEvent = new Event();
			newEvent.setAttacker(attacker);
			newEvent.setVictim(victim);

			EventCode eventCode = new EventCode();

			String prefix = "";
			if ((victim.getTeam() != null)
					&& victim.getTeam().equals(attacker.getTeam())) {
				prefix = "TK:";
				eventCode.setCode(EventCode.EVENT_CODE_TEAMKILL);
			} else {
				eventCode.setCode(EventCode.EVENT_CODE_KILL);
			}

			newEvent.setEventCode(eventCode);

			newEvent.setEventText(String.format("{attacker} [%s%s] {victim}",
					prefix, weapon));

			TransportMessage transportMessage = new TransportMessage();
			transportMessage.setEvent(newEvent);
			Round round = roundManager.getCurrentRound();
			if (round != null) {
				transportMessage.setSessionId(round.getId());
			}
			messageDestination.sendMessage(transportMessage);

			return;
		}
	}
}
