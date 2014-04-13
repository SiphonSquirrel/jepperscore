package jepperscore.scraper.common.query.sourceengine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jepperscore.dao.model.Alias;
import jepperscore.dao.model.Score;
import jepperscore.scraper.common.query.QueryCallbackInfo;
import jepperscore.scraper.common.query.QueryClientListener;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This tests the {@link SourceEngineQueryClient}.
 *
 * @author Chuck
 *
 */
public class SourceEngineQueryClientTest {

	/**
	 * This class stands up a fake gamespy test server.
	 *
	 * @author Chuck
	 *
	 */
	private static class SourceEngineTestServer implements Runnable {
		/**
		 * The logger for this class.
		 */
		private static final Logger LOG = LoggerFactory
				.getLogger(SourceEngineTestServer.class);

		/**
		 * The UDP socket.
		 */
		private DatagramSocket server;

		/**
		 * The info response.
		 */
		private byte[] infoResponse = new byte[0];

		/**
		 * The rules response.
		 */
		private byte[] rulesResponse = new byte[0];

		/**
		 * The players response.
		 */
		private byte[] playersResponse = new byte[0];

		/**
		 * Player serialized lines.
		 */
		private List<byte[]> playerLines = new LinkedList<byte[]>();

		/**
		 * This constructor creates the server on a random port.
		 *
		 * @throws SocketException
		 *             If there was an issue creating the server.
		 */
		public SourceEngineTestServer() throws SocketException {
			server = new DatagramSocket(0);
		}

		/**
		 * @return The port the server was started on.
		 */
		public int getPort() {
			return server.getLocalPort();
		}

		/**
		 * Sets the info response.
		 * @param protocolVersion
		 * @param hostName
		 * @param mapName
		 * @param gameDirectory
		 * @param gameDescription
		 * @param steamApplicationID
		 * @param playerCount
		 * @param maxPlayers
		 * @param botCount
		 * @param serverType
		 * @param serverOS
		 * @param passworded
		 * @param secureServer
		 */
		public void setInfoResponse(byte protocolVersion,
				String hostName,
				String mapName,
				String gameDirectory,
				String gameDescription,
				int steamApplicationID,
				byte playerCount,
				byte maxPlayers,
				byte botCount,
				char serverType,
				char serverOS,
				boolean passworded,
				boolean secureServer) {
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			buffer.put(SourceEngineQueryClient.MESSAGE_HEADER);
			buffer.put((byte) 'I');
			buffer.put(protocolVersion);

			buffer.put(hostName.getBytes(StandardCharsets.UTF_8)).put((byte) 0);
			buffer.put(mapName.getBytes(StandardCharsets.UTF_8)).put((byte) 0);
			buffer.put(gameDirectory.getBytes(StandardCharsets.UTF_8)).put((byte) 0);
			buffer.put(gameDescription.getBytes(StandardCharsets.UTF_8)).put((byte) 0);

			buffer.putInt(0);
			buffer.put(playerCount);
			buffer.put(maxPlayers);
			buffer.put(botCount);
			buffer.put((byte) serverType);
			buffer.put((byte) serverOS);
			buffer.put((byte) (passworded ? 1 : 0));
			buffer.put((byte) (secureServer ? 1 : 0));

			infoResponse = new byte[buffer.position() + 1];
			buffer.rewind();
			buffer.get(infoResponse);
		}

		/**
		 * Sets the rules response.
		 * @param rules The rules response.
		 */
		public void setRulesResponse(Map<String, String> rules) {
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			buffer.put(SourceEngineQueryClient.MESSAGE_HEADER);
			buffer.put((byte) 'E');
			buffer.putShort((short) rules.size());
			for (Entry<String, String> entry : rules.entrySet()) {
				buffer.put(entry.getKey().getBytes(StandardCharsets.UTF_8)).put((byte) 0);
				buffer.put(entry.getValue().getBytes(StandardCharsets.UTF_8)).put((byte) 0);
			}

			rulesResponse = new byte[buffer.position() + 1];
			buffer.rewind();
			buffer.get(rulesResponse);
		}

		/**
		 * Adds a player to the players response.
		 * @param playerName The player name.
		 * @param playerScore Their score.
		 * @param playerTime How long they've played...?
		 */
		public void addPlayerResponse(String playerName,
				int playerScore,
				float playerTime) {
			ByteBuffer playerBuffer = ByteBuffer.allocate(1024);
			playerBuffer.order(ByteOrder.LITTLE_ENDIAN);
			playerBuffer.put(playerName.getBytes(StandardCharsets.UTF_8)).put((byte) 0);
			playerBuffer.putInt(playerScore);
			playerBuffer.putFloat(playerTime);

			byte[] playerLine = new byte[playerBuffer.position()];
			playerBuffer.rewind();
			playerBuffer.get(playerLine);

			playerLines.add(playerLine);

			ByteBuffer buffer = ByteBuffer.allocate(1024);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			buffer.put(SourceEngineQueryClient.MESSAGE_HEADER);
			buffer.put((byte) 'D');
			buffer.put((byte) playerLines.size());

			int i = 0;
			for (byte[] line: playerLines) {
				buffer.put((byte) i);
				buffer.put(line);
				i++;
			}

			playersResponse = new byte[buffer.position() + 1];
			buffer.rewind();
			buffer.get(playersResponse);
		}

		@Override
		public void run() {
			byte[] recvBuffer = new byte[1024 * 8];
			while (true) {
				DatagramPacket recvPacket = new DatagramPacket(recvBuffer,
						recvBuffer.length);
				try {
					server.receive(recvPacket);
					byte[] recvData = recvPacket.getData();
					for (int i = 0; i < 4; i++) {
						if (recvData[i] != (byte) 0xFF) {
							LOG.error("Byte " + i + " of request was "
									+ recvData[i] + ", 0xFF expected.");
							continue;
						}
					}

					if (recvData.length < 5) {
						LOG.error("Not enough data! Got data length of "
								+ recvData.length);
						continue;
					}

					char request = (char) recvData[4];
					byte[] response = null;
					if (request == 'T') {
						response = infoResponse;
					} else if (request == 'V') {
						response = rulesResponse;
					} else if (request == 'U') {
						response = playersResponse;
					}

					if (response != null) {
						server.send(new DatagramPacket(response,
								response.length, recvPacket.getAddress(),
								recvPacket.getPort()));
					} else {
						LOG.error("Got unexpected request: " + request);
					}
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
					break;
				}
			}
		}
	}

	/**
	 * Tracks the returned info from the other thread.
	 */
	private volatile QueryCallbackInfo returnedInfo = null;

	/**
	 * Tracks the returned rules from the other thread.
	 */
	private volatile QueryCallbackInfo returnedRules = null;

	/**
	 * Tracks the returned players from the other thread.
	 */
	private volatile QueryCallbackInfo returnedPlayers = null;

	/**
	 * This tests the {@link SourceEngineQueryClient}.
	 *
	 * @throws IOException
	 *             If something goes boom!
	 */
	@Test
	public void test() throws IOException {
		SourceEngineTestServer server = new SourceEngineTestServer();
		Thread serverThread = new Thread(server);
		serverThread.start();

		byte protocolVersion = 3;
		String hostName = "Host Name";
		String mapName = "Map Name";
		String gameDirectory = "Game Directory";
		String gameDescription = "Game Description";
		int steamApplicationId = 0;
		byte currentPlayers = 3;
		byte maxPlayers = 16;
		byte botCount = 3;
		char serverType = SourceEngineQueryClient.SERVER_TYPE_DEDICATED;
		char serverOS = SourceEngineQueryClient.SERVER_OS_LINUX;
		boolean passworded = false;
		boolean secure = true;
		server.setInfoResponse(protocolVersion, hostName, mapName, gameDirectory, gameDescription, steamApplicationId,
				currentPlayers, maxPlayers, botCount, serverType,
				serverOS, passworded, secure);

		Map<String, String> rules = new HashMap<String, String>();
		rules.put("mp_timelimit", "0");
		rules.put("mp_friendlyfire", "0");
		rules.put("mp_falldamage", "0");
		rules.put("mp_weaponstay", "0");
		rules.put("mp_forcerespawn", "1");
		server.setRulesResponse(rules);

		server.addPlayerResponse("Player1", 1, 1.0f);
		server.addPlayerResponse("Player2", 2, 2.0f);
		server.addPlayerResponse("Player3", 3, 3.0f);

		SourceEngineQueryClient client = new SourceEngineQueryClient("localhost", server.getPort());

		client.registerListener("info", new QueryClientListener() {

			@Override
			public void queryClient(QueryCallbackInfo info) {
				if (returnedInfo == null) {
					returnedInfo = info;
				}
			}
		});

		client.registerListener("rules", new QueryClientListener() {

			@Override
			public void queryClient(QueryCallbackInfo info) {
				if (returnedRules == null) {
					returnedRules = info;
				}
			}
		});

		client.registerListener("players", new QueryClientListener() {

			@Override
			public void queryClient(QueryCallbackInfo info) {
				if (returnedPlayers == null) {
					returnedPlayers = info;
				}
			}
		});
		client.start();

		int i = 60;
		while ((i > 0) && ((returnedInfo == null) || (returnedRules == null) || (returnedPlayers == null))) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Do nothing!
			}
			i--;
		}
		assertNotEquals(0, i);

		assertEquals(hostName, returnedInfo.getServerMetadata().getServerName());
		assertEquals(mapName, returnedInfo.getServerMetadata().getMetadata().get("mapName"));
		assertEquals(gameDirectory, returnedInfo.getServerMetadata().getMetadata().get("gameDirectory"));
		assertEquals(gameDescription, returnedInfo.getServerMetadata().getMetadata().get("gameDescription"));
		assertEquals(steamApplicationId, Integer.parseInt(returnedInfo.getServerMetadata().getMetadata().get("steamApplicationID")));
		assertEquals(currentPlayers, Byte.parseByte(returnedInfo.getServerMetadata().getMetadata().get("playerCount")));
		assertEquals(maxPlayers, Byte.parseByte(returnedInfo.getServerMetadata().getMetadata().get("maxPlayers")));
		assertEquals(botCount, Byte.parseByte(returnedInfo.getServerMetadata().getMetadata().get("botCount")));
		assertEquals(serverType, returnedInfo.getServerMetadata().getMetadata().get("serverType").charAt(0));
		assertEquals(serverOS, returnedInfo.getServerMetadata().getMetadata().get("serverOS").charAt(0));
		assertEquals(passworded, Boolean.parseBoolean(returnedInfo.getServerMetadata().getMetadata().get("passworded")));
		assertEquals(secure, Boolean.parseBoolean(returnedInfo.getServerMetadata().getMetadata().get("secureServer")));

		assertEquals(rules.get("mp_timelimit"), returnedRules.getServerMetadata().getMetadata().get("mp_timelimit"));
		assertEquals(rules.get("mp_friendlyfire"), returnedRules.getServerMetadata().getMetadata().get("mp_friendlyfire"));
		assertEquals(rules.get("mp_falldamage"), returnedRules.getServerMetadata().getMetadata().get("mp_falldamage"));
		assertEquals(rules.get("mp_weaponstay"), returnedRules.getServerMetadata().getMetadata().get("mp_weaponstay"));
		assertEquals(rules.get("mp_forcerespawn"), returnedRules.getServerMetadata().getMetadata().get("mp_forcerespawn"));

		boolean player1 = false;
		boolean player2 = false;
		boolean player3 = false;

		for (Alias a: returnedPlayers.getPlayers()) {
			if ("Player1".equals(a.getName())) {
				assertFalse(player1);
				player1 = true;
				assertEquals("0", a.getId());
			}
			if ("Player2".equals(a.getName())) {
				assertFalse(player2);
				player2 = true;
				assertEquals("1", a.getId());
			}
			if ("Player3".equals(a.getName())) {
				assertFalse(player3);
				player3 = true;
				assertEquals("2", a.getId());
			}
		}

		assertTrue(player1);
		assertTrue(player2);
		assertTrue(player3);

		boolean score1 = false;
		boolean score2 = false;
		boolean score3 = false;

		for (Score s: returnedPlayers.getScores()) {
			if ("Player1".equals(s.getAlias().getName())) {
				assertFalse(score1);
				score1 = true;
				assertEquals("0", s.getAlias().getId());
				assertEquals(1.0f, s.getScore(), 0.1f);
			}
			if ("Player2".equals(s.getAlias().getName())) {
				assertFalse(score2);
				score2 = true;
				assertEquals("1", s.getAlias().getId());
				assertEquals(2.0f, s.getScore(), 0.1f);
			}
			if ("Player3".equals(s.getAlias().getName())) {
				assertFalse(score3);
				score3 = true;
				assertEquals("2", s.getAlias().getId());
				assertEquals(3.0f, s.getScore(), 0.1f);
			}
		}

		assertTrue(score1);
		assertTrue(score2);
		assertTrue(score3);
	}

}
