package jepperscore.scraper.common.query.gamespy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
	 * The info message.
	 */
	private static final byte[] INFO_MESSAGE = "\\info\\".getBytes();

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
	public GamespyQueryClient(String host, int port) throws IOException {
		this.port = port;

		address = InetAddress.getByName(host);
		socket = new DatagramSocket();
	}

	@Override
	protected void query() {
		byte[] recvBuffer = new byte[1024 * 8];
		DatagramPacket packet = new DatagramPacket(INFO_MESSAGE,
				INFO_MESSAGE.length, address, port);
		try {
			socket.setSoTimeout(1000);
			socket.send(packet);

			DatagramPacket recvPacket = new DatagramPacket(recvBuffer,
					recvBuffer.length);

			Map<Float, byte[]> data = new TreeMap<Float, byte[]>();
			int totalSize = 0;
			while (true) {
				try {
					socket.receive(recvPacket);
					data.put(0.0f, recvPacket.getData());
					totalSize += recvPacket.getLength();
					socket.setSoTimeout(10);
				} catch (IOException e) {
					break;
				}
			}

			ByteArrayOutputStream entireMessage = new ByteArrayOutputStream(
					totalSize);
			for (byte[] buf : data.values()) {
				entireMessage.write(buf);
			}

			String message = new String(entireMessage.toByteArray(), 1,
					entireMessage.size() - 1);
			String[] messageArray = message.split("\\\\");

			Map<String, String> values = new HashMap<String, String>();

			for (int i = 0; i < messageArray.length; i += 2) {
				values.put(messageArray[i], messageArray[i + 1]);
			}
			
			GamespyQueryCallbackInfo info = new GamespyQueryCallbackInfo();
			makeCallbacks(info);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

}
