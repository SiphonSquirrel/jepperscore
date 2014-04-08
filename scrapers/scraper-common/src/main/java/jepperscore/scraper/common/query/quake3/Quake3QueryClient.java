package jepperscore.scraper.common.query.quake3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
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
 * http://int64.org/docs/gamestat-protocols/quake3.html
 *
 * @author Chuck
 *
 */
public class Quake3QueryClient extends AbstractQueryClient {

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(Quake3QueryClient.class);

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
	 * Send/Recv Packet header.
	 */
	static final byte[] HEADER = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };

	/**
	 * The character set to do all the encoding.
	 */
	public static final Charset CHARSET = Charset.forName("UTF-8");

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
	public Quake3QueryClient(String host, int port)
			throws IOException {
		this.port = port;

		address = InetAddress.getByName(host);
		socket = new DatagramSocket();
	}

	@Override
	protected void query(String queryType) {
		if ("status".equals(queryType)) {
			byte[] requestBytes = ("get" + queryType + "\n").getBytes(CHARSET);

			ByteBuffer sendBuffer = ByteBuffer.allocate(4 + requestBytes.length);
			sendBuffer.put(HEADER);
			sendBuffer.put(requestBytes);

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

				QueryCallbackInfo queryInfo = new QueryCallbackInfo();

				byte[] recvData = recvPacket.getData();
				String[] arr = new String(recvData, HEADER.length, recvData.length - HEADER.length, CHARSET).split("\n");
				if (arr.length < 2) {
					LOG.error("Unexpected line count returned! " + arr.length);
					return;
				}

				if (!arr[0].equals(queryType + "Response")) {
					LOG.error("Got back unexpected response:" + arr[0]);
					return;
				}

				ServerMetadata serverMetadata = new ServerMetadata();
				queryInfo.setServerMetadata(serverMetadata);

				Map<String, String> metadata = new HashMap<String, String>();
				serverMetadata.setMetadata(metadata);

				String[] serverInfo = arr[1].split("\\\\");
				for (int i = 1; i < (serverInfo.length - 1); i += 2) {
					String key = serverInfo[i];
					String value = serverInfo[i + 1];
					switch (key) {
					case "sv_hostname":
						serverMetadata.setServerName(value);
						break;
					default:
						metadata.put(key, value);
						break;
					}
				}

				for (int i = 2; i < (arr.length - 1); i++) {
					String line = arr[i];

					String scoreStr = line.substring(0, line.indexOf(' '));
					String name = line.substring(line.indexOf('"') + 1, line.length() - 1);

					Alias alias = new Alias();
					alias.setName(name);
					Score s = new Score(alias, Float.parseFloat(scoreStr));

					queryInfo.getPlayers().add(alias);
					queryInfo.getScores().add(s);
				}

				makeCallbacks(queryType, queryInfo);
			} catch (SocketTimeoutException e) {
				// Do nothing!
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}
}
