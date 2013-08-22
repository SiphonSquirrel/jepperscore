package jepperscore.scraper.common.query.idtech;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import jepperscore.dao.model.Alias;
import jepperscore.dao.model.Score;
import jepperscore.dao.model.ServerMetadata;
import jepperscore.dao.model.Team;
import jepperscore.scraper.common.PlayerManager;
import jepperscore.scraper.common.query.AbstractQueryClient;
import jepperscore.scraper.common.query.QueryCallbackInfo;
import jepperscore.scraper.common.query.gamespy.GamespyQueryClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class queries using the GameSpy protocol.
 * http://jmp.kapsi.fi/etqw/getinfo/
 *
 * @author Chuck
 *
 */
public class IdTech4QueryClient extends AbstractQueryClient {

	/**
	 * The logger for this class.
	 */
	private static final Logger LOG = LoggerFactory
			.getLogger(GamespyQueryClient.class);

	/**
	 * The character set to use when translating from bytes to string.
	 */
	private Charset charset;

	/**
	 * The query port.
	 */
	private int port;

	/**
	 * The query address.
	 */
	private InetAddress address;

	/**
	 * The query socket.
	 */
	private DatagramSocket socket;

	/**
	 * The player manager.
	 */
	private PlayerManager playerManager;

	/**
	 * The score mode.
	 */
	private IdTechScoreMode scoreMode = IdTechScoreMode.Kills;

	/**
	 * This constructor sets up the query client.
	 *
	 * @param host
	 *            The host to query.
	 * @param port
	 *            The port to query.
	 * @param playerManager
	 *            The player manager to use.
	 * @throws IOException
	 *             Thrown when there is a problem setting up the socket.
	 */
	public IdTech4QueryClient(String host, int port, PlayerManager playerManager)
			throws IOException {
		this.port = port;
		this.playerManager = playerManager;

		address = InetAddress.getByName(host);
		socket = new DatagramSocket();

		setCharset(StandardCharsets.UTF_8);
	}

	@Override
	protected void query(@Nonnull String queryType) {
		final byte[] challange = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05,
				0x06, 0x07, 0x08 };

		String queryString = "get" + queryType.substring(0, 1).toUpperCase()
				+ queryType.substring(1) + '\0';

		byte[] requestBytes = queryString.getBytes(charset);

		ByteBuffer sendBuffer = ByteBuffer.allocate(10 + requestBytes.length);

		sendBuffer.put((byte) 0xFF);
		sendBuffer.put((byte) 0xFF);
		sendBuffer.put(requestBytes);
		sendBuffer.put(challange);

		DatagramPacket packet = new DatagramPacket(sendBuffer.array(),
				sendBuffer.capacity(), address, port);
		try {
			socket.setSoTimeout(1000);
			socket.send(packet);

			ByteBuffer recvBuffer = ByteBuffer.allocate(1024 * 8);
			recvBuffer.order(ByteOrder.LITTLE_ENDIAN);
			DatagramPacket recvPacket = new DatagramPacket(recvBuffer.array(),
					recvBuffer.capacity());
			socket.receive(recvPacket);

			if ((recvBuffer.get() != (byte) 0xFF)
					|| (recvBuffer.get() != (byte) 0xFF)) {
				return;
			}

			byte[] responseBytes = (queryType + "Response" + '\0')
					.getBytes(charset);
			byte[] tmpBuffer = new byte[responseBytes.length];

			recvBuffer.get(tmpBuffer);
			if (!Arrays.equals(responseBytes, tmpBuffer)) {
				return;
			}

			tmpBuffer = new byte[8];
			recvBuffer.get(tmpBuffer);
			if (!Arrays.equals(challange, tmpBuffer)) {
				return;
			}

			QueryCallbackInfo info = new QueryCallbackInfo();

			int major = recvBuffer.getShort();
			int minor = recvBuffer.getShort();

			boolean knownVersion = false;

			switch (major) {
			case 21:
				switch (minor) {
				case 10:
					knownVersion = true;
					break;
				default:
					break;
				}
			default:
				break;
			}

			if (!knownVersion) {
				LOG.warn("Unfamiliar version... attempting to parse anyway.");
			}

			// Skip size
			recvBuffer.position(recvBuffer.position() + 4);

			// Read values
			ServerMetadata metadata = new ServerMetadata();
			Map<String, String> headers = new HashMap<String, String>();
			String key = "";
			String value = "";
			do {
				key = readString(recvBuffer);
				value = readString(recvBuffer);
				switch (key) {
				case "":
					break;
				case "si_name":
					metadata.setServerName(value);
					break;
				default:
					headers.put(key, value);
				}
			} while (!key.isEmpty() && !value.isEmpty());
			metadata.setMetadata(headers);

			// Read Players
			Map<Integer, Alias> players = new HashMap<Integer, Alias>();
			while (true) {
				int id = recvBuffer.get();

				if (id == 32) {
					break;
				}

				// Skip ping & rate
				recvBuffer.position(recvBuffer.position() + 2);

				String playerName = readString(recvBuffer);
				boolean tagIsSuffix = recvBuffer.get() == 1;
				String clanTag = readString(recvBuffer);
				boolean isBot = recvBuffer.get() == 1;

				if (!clanTag.isEmpty()) {
					if (tagIsSuffix) {
						playerName += " " + clanTag;
					} else {
						playerName = clanTag + " " + playerName;
					}
				}

				Alias player = new Alias();
				player.setId("" + id);
				player.setName(playerName);
				player.setBot(isBot);

				players.put(id, playerManager.providePlayerRecord(player));
			}

			if (queryType.equals("infoEx")) {
				// Read server info
				// Skip os mask, ranked, time left & game state
				recvBuffer.position(recvBuffer.position() + 10);
				int serverType = recvBuffer.get();
				if (serverType == 0) { // Regular server
					recvBuffer.position(recvBuffer.position() + 1);
				} else { // TV server
					recvBuffer.position(recvBuffer.position() + 8);
				}

				List<Score> scores = new LinkedList<Score>();
				Map<String, Team> teams = new HashMap<String, Team>();
				for (int i = 0; i < players.size(); i++) {
					int id = recvBuffer.get();

					float experience = recvBuffer.getFloat();
					String teamName = readString(recvBuffer);
					int kills = recvBuffer.getInt();
					int deaths = recvBuffer.getInt();

					Team team = teams.get(teamName);
					if (team == null) {
						team = new Team(teamName);
						teams.put(teamName, team);
					}

					Alias player = players.get(id);
					if (player != null) {
						Score s = new Score();
						s.setAlias(player);
						switch (scoreMode) {
						case Experience:
							s.setScore(experience);
							break;
						case Kills:
							s.setScore(kills);
							break;
						case KillsMinusDeaths:
							s.setScore(kills - deaths);
						}
						scores.add(s);
					}
				}
				info.setScores(scores);
			}

			info.setServerMetadata(metadata);
			info.setPlayers(players.values());

			makeCallbacks(queryType, info);
		} catch (java.net.SocketTimeoutException e) {
			// Do nothing!
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	/**
	 * Reads a string from a {@link ByteBuffer}.
	 *
	 * @param buffer
	 *            The buffer to read from.
	 * @return The string that was read.
	 */
	private String readString(ByteBuffer buffer) {
		byte[] bytes = new byte[buffer.remaining()];
		int count = 0;

		byte b;
		while ((b = buffer.get()) != 0) {
			bytes[count] = b;
			count++;
		}

		return new String(bytes, 0, count, charset);
	}

	/**
	 * @return The charset used to translate bytes to/from strings.
	 */
	public Charset getCharset() {
		return charset;
	}

	/**
	 * @param charset
	 *            The charset used to translate bytes to/from strings.
	 */
	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	/**
	 * @return The score mode.
	 */
	public IdTechScoreMode getScoreMode() {
		return scoreMode;
	}

	/**
	 * @param scoreMode The score mode to use.
	 */
	public void setScoreMode(IdTechScoreMode scoreMode) {
		this.scoreMode = scoreMode;
	}

}
