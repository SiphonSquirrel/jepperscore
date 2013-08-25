package jepperscore.scraper.common.query.gamespy;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

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
	 * Registered message splitters.
	 */
	private Map<String, GamespyMessageSplitter> splitters = new HashMap<String, GamespyMessageSplitter>();

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
	public GamespyQueryClient(String host, int port)
			throws IOException {
		this.port = port;

		address = InetAddress.getByName(host);
		socket = new DatagramSocket();

		setCharset(StandardCharsets.UTF_8);
	}

	/**
	 * Registers a splitter.
	 *
	 * @param queryType
	 *            The query type to split.
	 * @param splitter
	 *            The splitter.
	 */
	public void registerMessageSplitter(String queryType,
			GamespyMessageSplitter splitter) {
		splitters.put(queryType, splitter);
	}

	/**
	 * Unregisters a message splitter.
	 *
	 * @param queryType
	 *            The query type to unregister.
	 */
	public void unregisterMessageSplitter(String queryType) {
		splitters.remove(queryType);
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

			GamespyMessageSplitter splitter = splitters.get(queryType);
			if (splitter != null) {
				GamespyQueryCallbackInfo info = splitter.splitMessage(queryType, messageArray);

				if (info != null) {
					info.setRawResponse(message);
					makeCallbacks(queryType, info);
				}
			} else {
				LOG.error("Unsure how to handle query type: " + queryType);
			}
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
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
