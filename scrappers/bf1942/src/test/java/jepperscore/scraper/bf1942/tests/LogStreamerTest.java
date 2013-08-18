/**
 *
 */
package jepperscore.scraper.bf1942.tests;

import static org.junit.Assert.assertEquals;

import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import javax.jms.Connection;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;

import jepperscore.dao.DaoConstant;
import jepperscore.scraper.bf1942.LogStreamer;
import jepperscore.scraper.common.PlayerManager;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Chuck
 *
 */
public class LogStreamerTest {

	/**
	 * The ActiveMQ connection factory.
	 */
	private ActiveMQConnectionFactory cf;

	/**
	 * The ActiveMQ connection.
	 */
	private Connection conn;

	/**
	 * The current ActiveMQ session.
	 */
	private Session session;

	/**
	 * The current ActiveMQ topic.
	 */
	private Topic eventTopic;

	/**
	 * This function sets up an embedded version of ActiveMQ for testing.
	 *
	 * @throws Exception
	 *             If something goes wrong in the setup.
	 */
	@Before
	public void setUp() throws Exception {
		cf = new ActiveMQConnectionFactory(
				"vm://localhost?broker.persistent=false");
		conn = cf.createConnection();
		conn.start();
		session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

		eventTopic = session.createTopic(DaoConstant.EVENT_TOPIC);
	}

	/**
	 * This function tears down the embedded version of ActiveMQ.
	 *
	 * @throws Exception
	 *             If something goes wrong in the tear down.
	 */
	@After
	public void tearDown() throws Exception {
		conn.stop();
	}

	/**
	 * Test method for {@link jepperscore.scraper.bf1942.LogStreamer}.
	 *
	 * @throws Exception
	 *             If a problem occurs during the test.
	 */
	@Test
	public void testLogStreamer() throws Exception {
		final List<Message> messages = new LinkedList<Message>();

		PipedOutputStream os = new PipedOutputStream();
		PipedInputStream is = new PipedInputStream(os, 1024 * 1024);

		MessageConsumer consumer = session.createConsumer(eventTopic);
		consumer.setMessageListener(new MessageListener() {

			@Override
			public void onMessage(Message message) {
				messages.add(message);
			}
		});

		PlayerManager playerManager = new PlayerManager(session, session.createProducer(eventTopic));

		LogStreamer ls = new LogStreamer(is, session, session.createProducer(eventTopic), playerManager);
		Thread lsThread = new Thread(ls);
		lsThread.start();

		Writer writer = new OutputStreamWriter(os);
		writer.write("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>"
				+ "<bf:log version=\"1.1\" xmlns:bf=\"http://www.dice.se/xmlns/bf/1.1\">");
		writer.flush();
		writer.write("<bf:round timestamp=\"6.49246\">");
		writer.flush();
		Thread.sleep(500);

		writer.write("<bf:server>"
				+ "<bf:setting name=\"server name\">Lan Party</bf:setting>"
				+ "<bf:setting name=\"port\">14567</bf:setting>"
				+ "<bf:setting name=\"dedicated\">1</bf:setting>"
				+ "<bf:setting name=\"modid\">bf1942</bf:setting>"
				+ "<bf:setting name=\"mapid\">BF1942</bf:setting>"
				+ "<bf:setting name=\"map\">berlin</bf:setting>"
				+ "<bf:setting name=\"game mode\">GPM_CQ</bf:setting>"
				+ "<bf:setting name=\"gametime\">0</bf:setting>"
				+ "<bf:setting name=\"maxplayers\">32</bf:setting>"
				+ "<bf:setting name=\"scorelimit\">0</bf:setting>"
				+ "<bf:setting name=\"norounds\">3</bf:setting>"
				+ "<bf:setting name=\"spawntime\">15</bf:setting>"
				+ "<bf:setting name=\"spawndelay\">5</bf:setting>"
				+ "<bf:setting name=\"gamestartdelay\">20</bf:setting>"
				+ "<bf:setting name=\"roundstartdelay\">10</bf:setting>"
				+ "<bf:setting name=\"soldierff\">100</bf:setting>"
				+ "<bf:setting name=\"vehicleff\">100</bf:setting>"
				+ "<bf:setting name=\"ticketratio\">100</bf:setting>"
				+ "<bf:setting name=\"internet\">0</bf:setting>"
				+ "<bf:setting name=\"alliedtr\">1</bf:setting>"
				+ "<bf:setting name=\"axistr\">1</bf:setting>"
				+ "<bf:setting name=\"coopskill\">50</bf:setting>"
				+ "<bf:setting name=\"coopcpu\">100</bf:setting>"
				+ "<bf:setting name=\"reservedslots\">0</bf:setting>"
				+ "<bf:setting name=\"allownosecam\">1</bf:setting>"
				+ "<bf:setting name=\"freecamera\">0</bf:setting>"
				+ "<bf:setting name=\"externalviews\">1</bf:setting>"
				+ "<bf:setting name=\"autobalance\">0</bf:setting>"
				+ "<bf:setting name=\"tagdistance\">100</bf:setting>"
				+ "<bf:setting name=\"tagdistancescope\">300</bf:setting>"
				+ "<bf:setting name=\"kickback\">0</bf:setting>"
				+ "<bf:setting name=\"kickbacksplash\">0</bf:setting>"
				+ "<bf:setting name=\"soldierffonsplash\">100</bf:setting>"
				+ "<bf:setting name=\"vehicleffonsplash\">100</bf:setting>"
				+ "<bf:setting name=\"hitindication\">1</bf:setting>"
				+ "<bf:setting name=\"tkpunish\">0</bf:setting>"
				+ "<bf:setting name=\"crosshairpoint\">1</bf:setting>"
				+ "<bf:setting name=\"deathcamtype\">0</bf:setting>"
				+ "<bf:setting name=\"contentcheck\">0</bf:setting>"
				+ "<bf:setting name=\"sv_punkbuster\">0</bf:setting>"
				+ "</bf:server>");
		writer.flush();
		Thread.sleep(500);

		writer.write("<bf:event name=\"roundInit\" timestamp=\"26.4804\">"
				+ "<bf:param type=\"int\" name=\"tickets_team1\">120</bf:param>"
				+ "<bf:param type=\"int\" name=\"tickets_team2\">120</bf:param>"
				+ "</bf:event>");
		writer.flush();
		Thread.sleep(500);

		writer.write("<bf:event name=\"spawnEvent\" timestamp=\"26.5537\">"
				+ "<bf:param type=\"int\" name=\"player_id\">255</bf:param>"
				+ "<bf:param type=\"vec3\" name=\"player_location\">1832.73/38.391/1859.79</bf:param>"
				+ "<bf:param type=\"int\" name=\"team\">1</bf:param>"
				+ "</bf:event>");
		writer.flush();
		Thread.sleep(500);

		writer.write("<bf:event name=\"pickupKit\" timestamp=\"26.5552\">"
				+ "<bf:param type=\"int\" name=\"player_id\">255</bf:param>"
				+ "<bf:param type=\"vec3\" name=\"player_location\">1832.73/38.391/1859.79</bf:param>"
				+ "<bf:param type=\"string\" name=\"kit\">German_AT</bf:param>"
				+ "</bf:event>");
		writer.flush();
		Thread.sleep(500);

		writer.write("<bf:event name=\"beginMedPack\" timestamp=\"34.4736\">"
				+ "<bf:param type=\"int\" name=\"player_id\">241</bf:param>"
				+ "<bf:param type=\"vec3\" name=\"player_location\">1846.55/39.4213/1869.73</bf:param>"
				+ "<bf:param type=\"int\" name=\"medpack_status\">1800</bf:param>"
				+ "<bf:param type=\"int\" name=\"healed_player\">233</bf:param>"
				+ "</bf:event>");
		writer.flush();
		Thread.sleep(500);

		writer.write("<bf:event name=\"endMedPack\" timestamp=\"34.5107\">"
				+ "<bf:param type=\"int\" name=\"player_id\">241</bf:param>"
				+ "<bf:param type=\"vec3\" name=\"player_location\">1846.55/39.4213/1869.73</bf:param>"
				+ "<bf:param type=\"int\" name=\"medpack_status\">1800</bf:param>"
				+ "</bf:event>");
		writer.flush();
		Thread.sleep(500);

		writer.write("<bf:event name=\"enterVehicle\" timestamp=\"35.6168\">"
				+ "<bf:param type=\"int\" name=\"player_id\">248</bf:param>"
				+ "<bf:param type=\"vec3\" name=\"player_location\">1798.02/39.0825/1906.75</bf:param>"
				+ "<bf:param type=\"string\" name=\"vehicle\">T34</bf:param>"
				+ "<bf:param type=\"int\" name=\"pco_id\">0</bf:param>"
				+ "<bf:param type=\"int\" name=\"is_default\">0</bf:param>"
				+ "<bf:param type=\"int\" name=\"is_fake\">0</bf:param>"
				+ "</bf:event>");
		writer.flush();
		Thread.sleep(500);

		writer.write("<bf:event name=\"destroyVehicle\" timestamp=\"37.4382\">"
				+ "<bf:param type=\"int\" name=\"player_id\">254</bf:param>"
				+ "<bf:param type=\"vec3\" name=\"player_location\">1816.46/39.1122/1902.8</bf:param>"
				+ "<bf:param type=\"string\" name=\"vehicle\">GermanSoldier</bf:param>"
				+ "<bf:param type=\"vec3\" name=\"vehicle_pos\">1822/39.3927/1868.28</bf:param>"
				+ "</bf:event>");
		writer.flush();
		Thread.sleep(500);

		writer.write("<bf:event name=\"scoreEvent\" timestamp=\"37.4383\">"
				+ "<bf:param type=\"int\" name=\"player_id\">254</bf:param>"
				+ "<bf:param type=\"vec3\" name=\"player_location\">1816.46/39.1122/1902.8</bf:param>"
				+ "<bf:param type=\"string\" name=\"score_type\">Kill</bf:param>"
				+ "<bf:param type=\"int\" name=\"victim_id\">244</bf:param>"
				+ "<bf:param type=\"string\" name=\"weapon\">(none)</bf:param>"
				+ "</bf:event>");
		writer.flush();
		Thread.sleep(500);

		writer.write("<bf:roundstats timestamp=\"980.609\">"
				+ "<bf:winningteam>2</bf:winningteam>"
				+ "<bf:victorytype>4</bf:victorytype>"
				+ "<bf:teamtickets team=\"1\">0</bf:teamtickets>"
				+ "<bf:teamtickets team=\"2\">15</bf:teamtickets>"
				+ "<bf:playerstat playerid=\"255\">"
				+ "<bf:statparam name=\"player_name\">Steffen R<bf:nonprint>252</bf:nonprint>hl</bf:statparam>"
				+ "<bf:statparam name=\"is_ai\">1</bf:statparam>"
				+ "<bf:statparam name=\"team\">1</bf:statparam>"
				+ "<bf:statparam name=\"score\">5</bf:statparam>"
				+ "<bf:statparam name=\"kills\">5</bf:statparam>"
				+ "<bf:statparam name=\"deaths\">7</bf:statparam>"
				+ "<bf:statparam name=\"tks\">0</bf:statparam>"
				+ "<bf:statparam name=\"captures\">0</bf:statparam>"
				+ "<bf:statparam name=\"attacks\">0</bf:statparam>"
				+ "<bf:statparam name=\"defences\">0</bf:statparam>"
				+ "<bf:statparam name=\"objectives\">0</bf:statparam>"
				+ "<bf:statparam name=\"objectivetks\">0</bf:statparam>"
				+ "</bf:playerstat>" + "</bf:roundstats>");
		writer.flush();
		Thread.sleep(500);

		writer.write("</bf:round>");
		writer.flush();
		writer.write("</bf:log>");
		writer.flush();
		Thread.sleep(500);

		assertEquals("Unexpected message count", 2, messages.size());
	}

}