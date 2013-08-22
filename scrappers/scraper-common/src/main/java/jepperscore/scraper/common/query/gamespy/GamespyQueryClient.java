package jepperscore.scraper.common.query.gamespy;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jepperscore.dao.model.Alias;
import jepperscore.dao.model.Score;
import jepperscore.dao.model.ServerMetadata;
import jepperscore.scraper.common.PlayerManager;
import jepperscore.scraper.common.query.AbstractQueryClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class queries using the GameSpy protocol.
 * http://int64.org/docs/gamestat-protocols/gamespy.html
 *
 * @author Chuck
 *
 */
public class GamespyQueryClient extends AbstractQueryClient {

	/**
	 * The logger for this class.
	 */
	private static final Logger LOG = LoggerFactory
			.getLogger(GamespyQueryClient.class);

	/**
	 * This is the query id tag used to join multiple query responses.
	 */
	private static final String QUERYID_TAG = "\\queryid\\";

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
	public GamespyQueryClient(String host, int port, PlayerManager playerManager)
			throws IOException {
		this.port = port;
		this.playerManager = playerManager;

		address = InetAddress.getByName(host);
		socket = new DatagramSocket();

		setCharset(StandardCharsets.UTF_8);
	}

	@Override
	protected void query(String queryType) {
		byte[] recvBuffer = new byte[1024 * 8];
		byte[] sendBuffer = ("\\" + queryType + "\\").getBytes(getCharset());

		DatagramPacket packet = new DatagramPacket(sendBuffer,
				sendBuffer.length, address, port);
		try {
			socket.setSoTimeout(1000);
			socket.send(packet);

			DatagramPacket recvPacket = new DatagramPacket(recvBuffer,
					recvBuffer.length);

			Map<Float, String> data = new TreeMap<Float, String>();
			while (true) {
				try {
					socket.receive(recvPacket);

					String stringData = new String(recvPacket.getData(), 0,
							recvPacket.getLength(), getCharset());
					int pos = stringData.indexOf(QUERYID_TAG);

					if (pos > 0) {
						int idStart = pos + QUERYID_TAG.length();
						int pos2 = stringData.indexOf("\\", idStart);
						if (pos2 < 0) {
							pos2 = stringData.length();
						}

						String id = stringData.substring(idStart, pos2);
						data.put(Float.parseFloat(id),
								stringData.substring(0, pos));
					}
					socket.setSoTimeout(10);
				} catch (IOException e) {
					break;
				}
			}

			StringBuilder entireMessage = new StringBuilder();
			for (String str : data.values()) {
				entireMessage.append(str);
			}

			String message = entireMessage.toString();
			String[] messageArray = message.split("\\\\");

			GamespyQueryCallbackInfo info = null;

			switch (queryType) {
			case "info":
				info = handleInfo(messageArray);
				break;
			case "players":
				info = handlePlayers(messageArray);
				break;
			default:
				LOG.error("Unsure how to handle query type: " + queryType);
				break;
			}

			if (info != null) {
				info.setRawResponse(message);
				makeCallbacks(queryType, info);
			}
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
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
		Map<String, Map<String, String>> playerInfo = new HashMap<String, Map<String, String>>();

		for (int i = 1; i < (messageArray.length - 1); i += 2) {
			String key = messageArray[i];
			String value = messageArray[i + 1];
			String index = "";

			Map<String, String> playerProperties = null;

			int pos = key.lastIndexOf("_");
			if (pos > 0) {
				index = key.substring(pos + 1);
				key = key.substring(0, pos);

				if (!key.equals("teamname")) {
					playerProperties = playerInfo.get(index);
					if (playerProperties == null) {
						playerProperties = new HashMap<String, String>();
						playerInfo.put(index, playerProperties);
					}
				} else {
					continue;
				}
			} else {
				continue;
			}

			playerProperties.put(key, value);
		}

		List<Score> scores = new LinkedList<Score>();

		for (Map<String, String> playerProperties : playerInfo.values()) {
			String name = playerProperties.get("playername");
			String scoreStr = playerProperties.get("score");
			if ((name != null) && (scoreStr != null)) {
				try {
					float scoreValue = Float.parseFloat(scoreStr);

					Alias player = playerManager.getPlayerByName(name);
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

		GamespyQueryCallbackInfo info = new GamespyQueryCallbackInfo();
		info.setScores(scores);

		return info;
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

}
