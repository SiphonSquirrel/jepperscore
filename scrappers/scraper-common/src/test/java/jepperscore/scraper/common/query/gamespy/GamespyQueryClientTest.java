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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GamespyQueryClientTest {

	private static class GamespyTestServer implements Runnable {
		private static final Logger LOG = LoggerFactory
				.getLogger(GamespyTestServer.class);

		private DatagramSocket server;
		private Map<String, String> responses = new HashMap<String, String>();

		public GamespyTestServer() throws SocketException {
			server = new DatagramSocket(0);
		}

		public int getPort() {
			return server.getLocalPort();
		}

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

	private DatagramSocket socket;

	private boolean gotReponse;

	@Before
	public void setUp() throws Exception {
		socket = new DatagramSocket(0);
	}

	@After
	public void tearDown() throws Exception {
		socket.close();
	}

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
					gotReponse = true;
					lockObject.notifyAll();
				}
			}
		});
		synchronized (lockObject) {
			queryClient.start();
			lockObject.wait(60000);
			assertTrue(gotReponse);
		}
	}

}
