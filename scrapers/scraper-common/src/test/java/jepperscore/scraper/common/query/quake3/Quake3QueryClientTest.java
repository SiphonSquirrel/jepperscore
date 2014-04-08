package jepperscore.scraper.common.query.quake3;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import jepperscore.dao.model.Score;
import jepperscore.scraper.common.query.QueryCallbackInfo;
import jepperscore.scraper.common.query.QueryClientListener;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Quake3QueryClientTest {

	/**
	 * This class stands up a fake gamespy test server.
	 * 
	 * @author Chuck
	 * 
	 */
	private static class Quake3TestServer implements Runnable {
		/**
		 * The logger for this class.
		 */
		private static final Logger LOG = LoggerFactory
				.getLogger(Quake3TestServer.class);

		/**
		 * The UDP socket.
		 */
		private DatagramSocket server;

		/**
		 * Responses to registered requests.
		 */
		private Map<String, byte[]> responses = new HashMap<String, byte[]>();

		/**
		 * This constructor creates the server on a random port.
		 * 
		 * @throws SocketException
		 *             If there was an issue creating the server.
		 */
		public Quake3TestServer() throws SocketException {
			server = new DatagramSocket(0);
		}

		/**
		 * @return The port the server was started on.
		 */
		public int getPort() {
			return server.getLocalPort();
		}

		/**
		 * This method matches a request to a response.
		 * 
		 * @param request
		 *            The request.
		 * @param response
		 *            The response to provide.
		 */
		public void setResponse(String request, String serverInfo, String[] playerList) {
			ByteBuffer response = ByteBuffer.allocate(1024 * 8);
			response.put(Quake3QueryClient.HEADER);
			response.put((request + "Response\n").getBytes());
			response.put((serverInfo + "\n").getBytes());
			
			for (String player: playerList) {
				response.put((player + "\n").getBytes());
			}

			responses.put(request, response.array());
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
						if (recvData[i] != (byte)0xFF) {
							LOG.error("Byte " + i +  " of request was " + recvData[i] + ", 0xFF expected.");
							continue;
						}
					}
					
					if (recvData.length < 8) {
						LOG.error("Not enough data! Got data length of " + recvData.length);
						continue;
					}
					
					String request = new String(recvData, 7,
							recvPacket.getLength() - 8);

					byte[] response = responses.get(request);
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

	private volatile QueryCallbackInfo returnedInfo = null;
	
	@Test
	public void test() throws IOException, InterruptedException {
		Quake3TestServer server = new Quake3TestServer();
		Thread serverThread = new Thread(server);
		serverThread.start();
		
		server.setResponse("status", "\\sv_hostname\\Game Name\\", new String[] {"1 0 \"Player1\"", "2 0 \"Player 20\""});
		
		Quake3QueryClient client = new Quake3QueryClient("localhost", server.getPort());
		
		client.registerListener("status", new QueryClientListener() {

			@Override
			public void queryClient(QueryCallbackInfo info) {
				returnedInfo = info;
			}
		});
		client.start();
		
		int i = 60;
		while ((i > 0) && (returnedInfo == null)) {
			Thread.sleep(1000);
			i--;
		}
		
		if (i == 0) {
			fail("Query response timed out.");
		}

		assertNotNull("Null server metadata!", returnedInfo.getServerMetadata());
		assertEquals("Expected only 2 players!", 2, returnedInfo.getScores().size());
		
		boolean player1seen = false;
		boolean player20seen = false;
		
		for (Score s: returnedInfo.getScores()) {
			switch (s.getAlias().getName()) {
			case "Player1": {
				assertEquals("Player1 score was not 1", 1.0f, s.getScore(), 0.1f);
				player1seen = true;
				break;
			}
			
			case "Player 20": {
				assertEquals("Player1 score was not 2", 2.0f, s.getScore(), 0.1f);
				player20seen = true;
				break;
			}
			default: {
				fail("Found unexpected player was: " + s.getAlias().getName());
				break;
			}
			}
		}
		
		assertEquals("Player1 was not seen", true, player1seen);
		assertEquals("Player 20 was not seen", true, player20seen);
		assertEquals("Incorrect game name", "Game Name", returnedInfo.getServerMetadata().getServerName());
	}

}
