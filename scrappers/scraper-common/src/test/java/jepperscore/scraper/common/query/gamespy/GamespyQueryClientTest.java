package jepperscore.scraper.common.query.gamespy;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import jepperscore.scraper.common.query.QueryCallbackInfo;
import jepperscore.scraper.common.query.QueryClientListener;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class tests the {@link GamespyQueryClient}.
 * @author Chuck
 *
 */
public class GamespyQueryClientTest {

	/**
	 * This class stands up a fake gamespy test server.
	 * @author Chuck
	 *
	 */
	private static class GamespyTestServer implements Runnable {
		/**
		 * The logger for this class.
		 */
		private static final Logger LOG = LoggerFactory
				.getLogger(GamespyTestServer.class);

		/**
		 * The UDP socket.
		 */
		private DatagramSocket server;
		
		/**
		 * Responses to registered requests.
		 */
		private Map<String, String> responses = new HashMap<String, String>();

		/**
		 * This constructor creates the server on a random port.
		 * @throws SocketException If there was an issue creating the server.
		 */
		public GamespyTestServer() throws SocketException {
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
		 * @param request The request.
		 * @param response The response to provide.
		 */
		public void setResponse(String request, String response) {
			responses.put(request, response);
		}

		@Override
		public void run() {
			byte[] recvBuffer = new byte[1024 * 8];
			while (true) {
				DatagramPacket recvPacket = new DatagramPacket(recvBuffer,
						recvBuffer.length);
				try {
					server.receive(recvPacket);
					String request = new String(recvPacket.getData(), 0,
							recvPacket.getLength());

					String response = responses.get(request);
					if (response != null) {
						byte[] sendBuffer = response.getBytes();
						server.send(new DatagramPacket(sendBuffer,
								sendBuffer.length, recvPacket.getAddress(),
								recvPacket.getPort()));
					}
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
					break;
				}
			}
		}

	}

	/**
	 * Keeps track if a response was received.
	 */
	private boolean gotReponse;

	/**
	 * This function tests the info request.
	 * @throws Exception If there was a problem during the test.
	 */
	@Test
	public void testInfo() throws Exception {
		final Object lockObject = new Object();

		GamespyTestServer server = new GamespyTestServer();
		Thread serverThread = new Thread(server);
		serverThread.start();

		server.setResponse(
				"\\info\\",
				"\\hostname\\|OoPS| Clan TO:AoT v315 Public Server-240\\hostport\\7777\\maptitle\\Deadly Drought\\mapname\\TO-Drought\\gametype\\s_SWATGame\\numplayers\\20\\maxplayers\\22\\gamemode\\openplaying\\gamever\\451\\minnetver\\432\\worldlog\\false\\wantworldlog\\false\\queryid\\29.1\\final\\");
		gotReponse = false;

		final GamespyQueryClient queryClient = new GamespyQueryClient(
				"localhost", server.getPort());
		queryClient.registerListener(new QueryClientListener() {

			@Override
			public void queryClient(QueryCallbackInfo info) {
				queryClient.stop();
				
				synchronized (lockObject) {
					assertEquals("|OoPS| Clan TO:AoT v315 Public Server-240", info.getServerMetadata().getServerName());
					
					gotReponse = true;
					lockObject.notifyAll();
				}
			}
		});
		synchronized (lockObject) {
			queryClient.start();
			lockObject.wait(60000);
			assertTrue("Reponse Timed out", gotReponse);
		}
	}

}
