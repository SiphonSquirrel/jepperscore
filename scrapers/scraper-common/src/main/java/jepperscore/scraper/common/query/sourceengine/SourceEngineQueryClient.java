package jepperscore.scraper.common.query.sourceengine;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import jepperscore.dao.model.Alias;
import jepperscore.dao.model.Score;
import jepperscore.dao.model.ServerMetadata;
import jepperscore.scraper.common.query.AbstractQueryClient;
import jepperscore.scraper.common.query.QueryCallbackInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This query client works with the Quake3 query protocol.
 * http://int64.org/docs/gamestat-protocols/source.html
 * https://developer.valvesoftware.com/wiki/Server_queries
 *
 * @author Chuck
 *
 */
public class SourceEngineQueryClient extends AbstractQueryClient {

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory
			.getLogger(SourceEngineQueryClient.class);

	/**
	 * The message header.
	 */
	static final byte[] MESSAGE_HEADER = new byte[] { (byte) 0xFF,
			(byte) 0xFF, (byte) 0xFF, (byte) 0xFF };

	/**
	 * Extra data for info query.
	 */
	static final byte[] INFO_EXTRA_DATA;

	/**
	 * Server type for listen servers.
	 */
	static final char SERVER_TYPE_LISTEN = 'l';

	/**
	 * Server type for dedicated servers.
	 */
	static final char SERVER_TYPE_DEDICATED = 'd';

	/**
	 * Server OS for Windows.
	 */
	static final char SERVER_OS_WINDOWS = 'w';

	/**
	 * Server OS for Linux.
	 */
	static final char SERVER_OS_LINUX = 'l';

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
	 * The player query challenge value.
	 */
	private volatile byte[] playerChallenge = null;

	/**
	 * The player query challenge value.
	 */
	private volatile byte[] rulesChallenge = null;

	static {
		byte[] infoHeader = "Source Engine Query"
				.getBytes(StandardCharsets.UTF_8);
		ByteBuffer extraData = ByteBuffer.allocate(infoHeader.length + 1);
		extraData.put(infoHeader).put((byte) 0);
		INFO_EXTRA_DATA = extraData.array();
	}

	/**
	 * This constructor sets up the query client.
	 *
	 * @param host
	 *            The host to query.
	 * @param port
	 *            The port to query.
	 * @throws IOException
	 *             Thrown when there is a problem setting up the socket.
	 */
	public SourceEngineQueryClient(String host, int port) throws IOException {
		this.port = port;
		address = InetAddress.getByName(host);
		socket = new DatagramSocket();
	}

	@Override
	protected void query(String queryType) {

		switch (queryType) {
		case "info": {
			queryInfo(queryType);
			break;
		}
		case "rules": {
			queryRules(queryType);
			break;
		}
		case "players": {
			queryPlayers(queryType);
			break;
		}
		default:
			LOG.warn("Cannot query for request:" + queryType);
			break;
		}
	}

	/**
	 * Makes the request for the specified query, returns a {@link ByteBuffer}.
	 *
	 * @param queryCode
	 *            The query code to send.
	 * @param expectedResultCode
	 *            The query code to expect.
	 * @param extraData
	 *            Any extra data to send with the response.
	 * @return The data in the packet.
	 */
	protected ByteBuffer makeRequest(char queryCode, char expectedResultCode,
			byte[] extraData) {
		ByteBuffer result = ByteBuffer.allocate(0);

		ByteBuffer sendBuffer = ByteBuffer.allocate(5 + (extraData == null ? 0
				: extraData.length));
		sendBuffer.put(MESSAGE_HEADER);
		sendBuffer.put((byte) queryCode);
		if (extraData != null) {
			sendBuffer.put(extraData);
		}

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

			byte[] recvData = recvPacket.getData();
			if (recvData.length < 5) {
				return result;
			}

			for (int i = 0; i < MESSAGE_HEADER.length; i++) {
				if (recvData[i] != MESSAGE_HEADER[i]) {
					return result;
				}
			}

			if (recvData[4] != (byte) expectedResultCode) {
				return result;
			}
			result = ByteBuffer.wrap(Arrays.copyOfRange(recvData, 5,
					recvPacket.getLength()));
			result.order(ByteOrder.LITTLE_ENDIAN);
		} catch (SocketTimeoutException e) {
			// Do nothing!
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}

		return result;
	}

	/**
	 * Makes a player challenge request.
	 */
	private synchronized void makePlayerChallengeRequest() {
		if (playerChallenge != null) {
			return;
		}

		byte[] initial = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
				(byte) 0xFF };

		ByteBuffer recvData = makeRequest('U', 'A', initial);
		playerChallenge = recvData.array();
	}

	/**
	 * Makes a player challenge request.
	 */
	private synchronized void makeRulesChallengeRequest() {
		if (rulesChallenge != null) {
			return;
		}

		byte[] initial = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
				(byte) 0xFF };

		ByteBuffer recvData = makeRequest('V', 'A', initial);
		rulesChallenge = recvData.array();
	}

	/**
	 * Reads a string from the buffer.
	 *
	 * @param buf
	 *            The buffer to read from.
	 * @return The read string.
	 */
	protected String readString(ByteBuffer buf) {
		for (int i = buf.position(); i < buf.capacity(); i++) {
			if (buf.get(i) == 0) {
				byte[] strBuf = new byte[i - buf.position()];
				buf.get(strBuf);
				buf.get(); // Skip the null byte
				return new String(strBuf, StandardCharsets.UTF_8);
			}
		}
		return "";
	}

	/**
	 * Sends an info query.
	 *
	 * @param queryType
	 *            The original passed in query.
	 */
	protected void queryInfo(String queryType) {

		ByteBuffer recvData = makeRequest('T', 'I', INFO_EXTRA_DATA);
		if (recvData.capacity() == 0) {
			return;
		}

		QueryCallbackInfo callbackInfo = new QueryCallbackInfo();
		ServerMetadata serverMetadata = new ServerMetadata();
		callbackInfo.setServerMetadata(serverMetadata);
		Map<String, String> metadata = new HashMap<String, String>();
		serverMetadata.setMetadata(metadata);

		byte version = recvData.get();
		if ((version != 3) && (version != 17)) {
			LOG.warn("Not familar with version " + version
					+ ", attempting parse anyway.");
		}

		serverMetadata.setServerName(readString(recvData));
		metadata.put("mapName", readString(recvData));
		metadata.put("gameDirectory", readString(recvData));
		metadata.put("gameDescription", readString(recvData));
		metadata.put("steamApplicationID",
				Integer.toString(recvData.getShort()));
		metadata.put("playerCount", Byte.toString(recvData.get()));
		metadata.put("maxPlayers", Byte.toString(recvData.get()));
		metadata.put("botCount", Byte.toString(recvData.get()));
		metadata.put("serverType", Character.toString((char) recvData.get()));
		metadata.put("serverOS", Character.toString((char) recvData.get()));
		metadata.put("passworded", Boolean.toString(recvData.get() != 0));
		metadata.put("secureServer", Boolean.toString(recvData.get() != 0));

		makeCallbacks(queryType, callbackInfo);
	}

	/**
	 * Sends an rules query.
	 *
	 * @param queryType
	 *            The original passed in query.
	 */
	protected void queryRules(String queryType) {
		if (rulesChallenge == null) {
			makeRulesChallengeRequest();
		}

		ByteBuffer recvData = makeRequest('V', 'E', rulesChallenge);
		if (recvData.capacity() == 0) {
			return;
		}

		QueryCallbackInfo callbackInfo = new QueryCallbackInfo();
		ServerMetadata serverMetadata = new ServerMetadata();
		callbackInfo.setServerMetadata(serverMetadata);
		Map<String, String> metadata = new HashMap<String, String>();
		serverMetadata.setMetadata(metadata);

		int count = recvData.getShort();
		for (int i = 0; i < count; i++) {
			metadata.put(readString(recvData), readString(recvData));
		}

		makeCallbacks(queryType, callbackInfo);
	}

	/**
	 * Sends an players query.
	 *
	 * @param queryType
	 *            The original passed in query.
	 */
	protected void queryPlayers(String queryType) {
		if (playerChallenge == null) {
			makePlayerChallengeRequest();
		}

		ByteBuffer recvData = makeRequest('U', 'D', playerChallenge);
		if (recvData.capacity() == 0) {
			return;
		}

		QueryCallbackInfo callbackInfo = new QueryCallbackInfo();

		int count = recvData.get();
		for (int i = 0; i < count; i++) {
			Alias player = new Alias();
			// Skip player chunk id
			recvData.get();
			player.setName(readString(recvData));

			Score score = new Score();
			score.setAlias(player);
			score.setScore(recvData.getInt());

			// Skip player time
			recvData.getFloat();

			callbackInfo.getPlayers().add(player);
			callbackInfo.getScores().add(score);
		}

		makeCallbacks(queryType, callbackInfo);

	}

}
