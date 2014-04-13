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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jepperscore.dao.model.Alias;
import jepperscore.dao.model.Score;
import jepperscore.dao.model.ServerMetadata;
import jepperscore.scraper.common.query.AbstractQueryClient;
import jepperscore.scraper.common.query.QueryCallbackInfo;

/**
 * This query client works with the Quake3 query protocol.
 * http://int64.org/docs/gamestat-protocols/source.html
 * 
 * @author Chuck
 * 
 */
public class SourceEngineQueryClient extends AbstractQueryClient {

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(SourceEngineQueryClient.class);

	/**
	 * The message header.
	 */
	public static final byte[] MESSAGE_HEADER = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };

	/**
	 * Server type for listen servers.
	 */
	public static final char SERVER_TYPE_LISTEN = 'l';

	/**
	 * Server type for dedicated servers.
	 */
	public static final char SERVER_TYPE_DEDICATED = 'd';

	/**
	 * Server OS for Windows.
	 */
	public static final char SERVER_OS_WINDOWS = 'w';

	/**
	 * Server OS for Linux.
	 */
	public static final char SERVER_OS_LINUX = 'l';

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
	 * This constructor sets up the query client.
	 * 
	 * @param host
	 *            The host to query.
	 * @param port
	 *            The port to query.
	 * @throws IOException
	 *             Thrown when there is a problem setting up the socket.
	 */
	public SourceEngineQueryClient(String host, int port)
			throws IOException {
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

	protected ByteBuffer makeRequest(char queryCode, char expectedResultCode) {
		ByteBuffer result = ByteBuffer.allocate(0);

		ByteBuffer sendBuffer = ByteBuffer.allocate(5);
		sendBuffer.put(MESSAGE_HEADER);
		sendBuffer.put((byte) queryCode);

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
			result = ByteBuffer.wrap(Arrays.copyOfRange(recvData, 5, recvPacket.getLength() - 1));
			result.order(ByteOrder.LITTLE_ENDIAN);
		} catch (SocketTimeoutException e) {
			// Do nothing!
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}

		return result;
	}

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

	protected void queryInfo(String queryType) {
		QueryCallbackInfo callbackInfo = new QueryCallbackInfo();
		ServerMetadata serverMetadata = new ServerMetadata();
		callbackInfo.setServerMetadata(serverMetadata);
		Map<String, String> metadata = new HashMap<String, String>();
		serverMetadata.setMetadata(metadata);

		ByteBuffer recvData = makeRequest('T', 'I');
		byte version = recvData.get();
		if (version != 3) {
			LOG.warn("Not familar with version " + version + ", attempting parse anyway.");
		}

		serverMetadata.setServerName(readString(recvData));
		metadata.put("mapName", readString(recvData));
		metadata.put("gameDirectory", readString(recvData));
		metadata.put("gameDescription", readString(recvData));
		metadata.put("steamApplicationID", Integer.toString(recvData.getInt()));
		metadata.put("playerCount", Byte.toString(recvData.get()));
		metadata.put("maxPlayers", Byte.toString(recvData.get()));
		metadata.put("botCount", Byte.toString(recvData.get()));
		metadata.put("serverType", Character.toString((char) recvData.get()));
		metadata.put("serverOS", Character.toString((char) recvData.get()));
		metadata.put("passworded", Boolean.toString(recvData.get() != 0));
		metadata.put("secureServer", Boolean.toString(recvData.get() != 0));
		
		makeCallbacks(queryType, callbackInfo);
	}

	protected void queryRules(String queryType) {
		QueryCallbackInfo callbackInfo = new QueryCallbackInfo();
		ServerMetadata serverMetadata = new ServerMetadata();
		callbackInfo.setServerMetadata(serverMetadata);
		Map<String, String> metadata = new HashMap<String, String>();
		serverMetadata.setMetadata(metadata);
		
		ByteBuffer recvData = makeRequest('V', 'E');
		
		int count = recvData.getShort();
		for (int i = 0; i < count; i++) {
			metadata.put(readString(recvData), readString(recvData));
		}
		
		makeCallbacks(queryType, callbackInfo);
	}

	protected void queryPlayers(String queryType) {
		QueryCallbackInfo callbackInfo = new QueryCallbackInfo();
		
		ByteBuffer recvData = makeRequest('U', 'D');
		
		int count = recvData.get();
		for (int i = 0; i < count; i++) {
			Alias player = new Alias();
			player.setId(Byte.toString(recvData.get()));
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
