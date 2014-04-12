package jepperscore.scraper.sourceengine.scraper;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import jepperscore.backends.testing.NullMessageDestination;
import jepperscore.scraper.common.SimpleDataManager;

import org.junit.Test;

/**
 * This class tests the {@link SourceEngineLogParser}.
 * @author Chuck
 *
 */
public class SourceEngineLogParserTest {

	/**
	 * Localhost as an InetAddress.
	 */
	private static final InetAddress LOCALHOST = InetAddress.getLoopbackAddress();

	/**
	 * Sends a log line.
	 * @param socket The socket to use.
	 * @param port The port to send to.
	 * @param line The line to send.
	 * @throws IOException If something goes boom.
	 */
	private static void sendLine(DatagramSocket socket, int port, String line) throws IOException {
		byte[] requestBytes = line.getBytes(StandardCharsets.UTF_8);
		ByteBuffer sendBuffer = ByteBuffer.allocate(requestBytes.length + SourceEngineLogParser.MESSAGE_HEADER.length);
		sendBuffer.put(SourceEngineLogParser.MESSAGE_HEADER);
		sendBuffer.put(requestBytes);
		DatagramPacket packet = new DatagramPacket(sendBuffer.array(),
				sendBuffer.capacity(), LOCALHOST, port);
		socket.send(packet);
	}

	/**
	 * This tests the {@link SourceEngineLogParser}.
	 * @throws IOException
	 */
	@Test
	public void test() throws IOException {
		NullMessageDestination dest = new NullMessageDestination("");
		SimpleDataManager dataManager = new SimpleDataManager(dest);

		SourceEngineLogParser parser = new SourceEngineLogParser("localhost", 0, dest, dataManager, dataManager);
		parser.start();
		int port = parser.getPort();

		DatagramSocket client = new DatagramSocket();
		sendLine(client, port, "RL 04/12/2014 - 13:51:27: \"ThatGuy<7><BOT><Red>\" killed \"CryBaby<6><BOT><Blue>\" with \"obj_sentrygun\" (attacker_position \"1418 2183 -231\") (victim_position \"1098 2388 -255\")");

		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}

	}

}
