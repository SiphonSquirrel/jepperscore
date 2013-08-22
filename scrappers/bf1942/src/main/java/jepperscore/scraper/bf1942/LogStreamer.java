package jepperscore.scraper.bf1942;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import jepperscore.dao.model.Alias;
import jepperscore.dao.model.Event;
import jepperscore.dao.model.EventCode;
import jepperscore.dao.model.Score;
import jepperscore.dao.model.Team;
import jepperscore.dao.transport.TransportMessage;
import jepperscore.scraper.common.MessageUtil;
import jepperscore.scraper.common.PlayerManager;
import jepperscore.scraper.common.ScoreManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class reads the BF1942 logs, and streams the events.
 *
 * @author Chuck
 *
 */
public class LogStreamer implements Runnable {

	/**
	 * Constant for the event type attribute.
	 */
	private final static String EVENT_TYPE_ATTRIBUTE = "name";

	/**
	 * The log for this class.
	 */
	private final static Logger LOG = LoggerFactory
			.getLogger(LogStreamer.class);

	/**
	 * The log stream.
	 */
	private InputStream stream;

	/**
	 * The current ActiveMQ session.
	 */
	private Session session;

	/**
	 * The current ActiveMQ MessageProducer.
	 */
	private MessageProducer producer;

	/**
	 * The {@link Charset} of the file.
	 */
	private Charset charset;

	/**
	 * The document builder.
	 */
	private DocumentBuilder dbBuilder;

	/**
	 * Used to signal when the log is closed.
	 */
	private boolean done = false;

	/**
	 * The player manager.
	 */
	private PlayerManager playerManager;

	/**
	 * The score manager.
	 */
	private ScoreManager scoreManager;

	/**
	 * This constructor points the log streamer at a log file.
	 *
	 * @param stream
	 *            The log stream to watch.
	 * @param session
	 *            The ActiveMQ {@link Session} to use.
	 * @param producer
	 *            The ActiveMQ {@link MessageProducer} to use.
	 * @param playerManager
	 *            The {@link PlayerManager} to use.
	 * @param scoreManager
	 *            The {@link ScoreManager} to use.
	 */
	public LogStreamer(@Nonnull InputStream stream, @Nonnull Session session,
			@Nonnull MessageProducer producer, PlayerManager playerManager,
			ScoreManager scoreManager) {
		this.stream = stream;
		this.session = session;
		this.producer = producer;
		this.playerManager = playerManager;
		this.scoreManager = scoreManager;
		this.charset = StandardCharsets.ISO_8859_1;

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			dbBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	/**
	 * Returns the latest XML snippet from the log file.
	 *
	 * @return The XML snippet.
	 * @throws IOException
	 *             When a problem occurs reading the newest entries.
	 */
	@CheckForNull
	private String getNewestEntries() throws IOException {
		StringBuilder newData = new StringBuilder();

		while (true) {
			int available = stream.available();

			if (available > 0) {
				byte[] buffer = new byte[available];
				stream.read(buffer);
				newData.append(new String(buffer, charset));
			} else {
				break;
			}
		}

		if (newData.length() == 0) {
			return null;
		}

		String data = newData.toString();

		if (data.contains("</bf:log>")) {
			done = true;
		}

		return closeHangingTags(data);
	}

	/**
	 * Closes any hanging tags left open by the beginning of a snippet.
	 *
	 * @param newData
	 *            The new XML string to close the tags for.
	 * @return The XML string with all the tags closed.
	 */
	@Nonnull
	private String closeHangingTags(@Nonnull String newData) {
		StringBuilder sb = new StringBuilder(newData);

		if (newData.contains("<bf:round ")) {
			if (!newData.contains("</bf:round")) {
				sb.append("</bf:round>");
			}
		} else {
			int pos = newData.indexOf("</bf:round>");
			if (pos >= 0) {
				sb.delete(pos, pos + "</bf:round>".length());
			}
		}

		if (!newData.contains("<bf:log")) {
			sb.insert(0, "<bf:log>");
		}

		if (!newData.contains("</bf:log>")) {
			sb.append("</bf:log>");
		}

		return sb.toString();
	}

	@Override
	public void run() {
		while (!done) {
			String xml = null;

			try {
				xml = getNewestEntries();
			} catch (IOException e1) {
				LOG.error(e1.getMessage(), e1);
				return;
			}

			if (xml != null) {
				Document doc = null;

				try (InputStream is = new ByteArrayInputStream(
						xml.getBytes(charset))) {
					doc = dbBuilder.parse(is);
				} catch (SAXException | IOException e) {
					LOG.error(e.getMessage(), e);
				}

				if (doc != null) {
					parseEvents(doc.getDocumentElement());
				}
			} else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					return;
				}
			}
		}
	}

	/**
	 * Parses the events from the XML document.
	 *
	 * @param topElement
	 *            The element to parse the events from.
	 */
	private void parseEvents(Element topElement) {
		NodeList nodes = topElement.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeType() == Element.ELEMENT_NODE) {
				Element element = (Element) node;

				switch (element.getNodeName()) {
				case "bf:round":
					playerManager.newRound();
					// We will also want to parse any child elements, as they
					// can get hidden on first run.
					parseEvents(element);
					break;

				case "bf:server":
					break;

				case "bf:event":
					handleEvent(element);
					break;

				case "bf:roundstats":
					handleRoundStats(element);
					break;

				default:
					LOG.info("Unhandled element: " + element.getNodeName());
				}
			}
		}
	}

	/**
	 * Returns the text of an element.
	 *
	 * @param element
	 *            The element to parse.
	 * @return The text.
	 */
	private String getElementText(@Nonnull Element element) {
		StringBuilder sb = new StringBuilder();
		NodeList childern = element.getChildNodes();
		for (int i = 0; i < childern.getLength(); i++) {
			Node child = childern.item(i);
			if (child.getNodeType() == Node.TEXT_NODE) {
				sb.append(child.getNodeValue());
			} else if (child.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) child;
				if ("bf:nonprint".equals(childElement.getNodeName())) {
					String charCodeStr = childElement.getFirstChild()
							.getNodeValue();
					try {
						byte charCode = (byte) (Integer.parseInt(charCodeStr) & 0xFF);
						sb.append(new String(new byte[] { charCode }, charset));
					} catch (NumberFormatException e) {
						LOG.error("Could not parse special character code as number: "
								+ charCodeStr);
					}
				}
			}
		}
		return sb.toString();
	}

	/**
	 * The function finds the passed element name with the name attribute =
	 * name.
	 *
	 * @param event
	 *            The parent element.
	 * @param name
	 *            The name.
	 * @param elementName
	 *            The parameter element to search for.
	 * @return The node value, null if the parameter was not found.
	 */
	@CheckForNull
	private String getParameter(@Nonnull Element event, String name,
			@Nonnull String elementName) {
		NodeList childern = event.getChildNodes();
		for (int i = 0; i < childern.getLength(); i++) {
			Node child = childern.item(i);
			if ((child.getNodeType() == Node.ELEMENT_NODE)
					&& (elementName.equals(child.getNodeName()))) {
				Element parameter = (Element) child;

				if (parameter.getAttribute("name").equals(name)) {
					return getElementText(parameter);
				}
			}
		}
		return null;
	}

	/**
	 * The function finds the <bf:param> with the name attribute = name.
	 *
	 * @param event
	 *            The parent element.
	 * @param name
	 *            The name.
	 * @return The node value, null if the parameter was not found.
	 */
	@CheckForNull
	private String getEventParameter(@Nonnull Element event, String name) {
		return getParameter(event, name, "bf:param");
	}

	/**
	 * The function finds the <bf:statparam> with the name attribute = name.
	 *
	 * @param event
	 *            The parent element.
	 * @param name
	 *            The name.
	 * @return The node value, null if the parameter was not found.
	 */
	@CheckForNull
	private String getRoundStatsParameter(@Nonnull Element event, String name) {
		return getParameter(event, name, "bf:statparam");
	}

	/**
	 * This function handles the parsed events.
	 *
	 * @param eventElement
	 *            The event element to parse.
	 */
	private void handleEvent(@Nonnull Element eventElement) {
		String eventType = eventElement.getAttribute(EVENT_TYPE_ATTRIBUTE);

		Event newEvent = null;
		EventCode eventCode = new EventCode();

		switch (eventType) {
		case "createPlayer": {
			String playerId = getEventParameter(eventElement, "player_id");
			String playerName = getEventParameter(eventElement, "name");
			String team = getEventParameter(eventElement, "team");

			if ((playerId != null) && (playerName != null) && (team != null)) {
				Alias newPlayer = new Alias();
				newPlayer.setId(playerId);
				newPlayer.setBot(false);
				newPlayer.setName(playerName);
				if ("1".equals(team)) {
					newPlayer.setTeam(new Team(BF1942Constants.AXIS_TEAM));
				} else if ("2".equals(team)) {
					newPlayer.setTeam(new Team(BF1942Constants.ALLIED_TEAM));
				} else {
					LOG.warn("Unrecognized team: " + team);
				}

				playerManager.providePlayerRecord(newPlayer);
			}

			break;
		}

		case "scoreEvent": {
			newEvent = new Event();

			String attackerId = getEventParameter(eventElement, "player_id");
			String victimId = getEventParameter(eventElement, "victim_id");
			String scoreType = getEventParameter(eventElement, "score_type");

			if (scoreType == null) {
				break;
			}

			if (attackerId != null) {
				newEvent.setAttacker(playerManager.getPlayer(attackerId));
			}

			if (victimId != null) {
				newEvent.setVictim(playerManager.getPlayer(victimId));
			}
			switch (scoreType) {
			case "TK":
			case "Kill": {
				String weapon = getEventParameter(eventElement, "weapon");
				if ("(none)".equals(weapon)) {
					weapon = "killed";
				}

				String prefix = "";
				if (scoreType.equals("TK")) {
					prefix = "TK:";
					eventCode.setCode("teamkill");
				} else {
					eventCode.setCode("kill");
				}

				newEvent.setEventText(String.format(
						"{attacker} [%s%s] {victim}", prefix, weapon));
				eventCode.setObject(weapon);
				newEvent.setEventCode(eventCode);

				Alias attacker = newEvent.getAttacker();
				if ((attacker != null) && Boolean.TRUE.equals(attacker.isBot())) {
					if (scoreType.equals("TK")) {
						scoreManager.incrementScore(attacker, -2);
					} else {
						scoreManager.incrementScore(attacker, 1);
					}
				}
				break;
			}

			case "Attack": {
				String point = "An unknown point";
				newEvent.setEventText(String.format(
						"%s was captured by {attacker}", point));
				eventCode.setCode("objective");
				eventCode.setObject(point);
				newEvent.setEventCode(eventCode);

				Alias attacker = newEvent.getAttacker();
				if ((attacker != null) && Boolean.TRUE.equals(attacker.isBot())) {
					scoreManager.incrementScore(attacker, 2);
				}
				break;
			}

			case "Death":
			case "DeathNoMsg":
				// Not important, ignore.
				break;

			default:
				LOG.info("Unhandled score type: " + scoreType);
				break;
			}

			break;
		}

		// We don't really care what kit, but we can use this information to
		// determine the team of bots.
		case "pickupKit": {
			String playerId = getEventParameter(eventElement, "player_id");
			String kit = getEventParameter(eventElement, "kit");
			if ((playerId == null) || (kit == null)) {
				break;
			}

			Alias player = playerManager.getPlayer(playerId);
			if (player == null) {
				break;
			}

			if (player.getTeam() != null) {
				break;
			}

			kit = kit.toUpperCase();

			if (kit.startsWith("GB") || kit.startsWith("US")
					|| kit.startsWith("RU")) {
				player.setTeam(new Team(BF1942Constants.ALLIED_TEAM));
			} else if (kit.startsWith("GE") || kit.startsWith("JA")) {
				player.setTeam(new Team(BF1942Constants.AXIS_TEAM));
			} else {
				LOG.warn("Unknown kit: " + kit);
				break;
			}

			playerManager.providePlayerRecord(player);
			break;
		}

		case "roundInit":
		case "spawnEvent":
		case "beginMedPack":
		case "endMedPack":
		case "enterVehicle":
		case "exitVehicle":
		case "destroyVehicle":
		case "beginRepair":
		case "endRepair":
			break;

		default:
			LOG.info("Unhandled event type: " + eventType);
		}

		if (newEvent != null) {
			TransportMessage transportMessage = new TransportMessage();
			transportMessage.setEvent(newEvent);

			MessageUtil.sendMessage(producer, session, transportMessage);
		}
	}

	/**
	 * This function handles the parsed round stats element.
	 *
	 * @param roundStatsElement
	 *            The round stats element to parse.
	 */
	private void handleRoundStats(@Nonnull Element roundStatsElement) {
		NodeList childern = roundStatsElement.getChildNodes();
		for (int i = 0; i < childern.getLength(); i++) {
			Node child = childern.item(i);
			if ((child.getNodeType() == Node.ELEMENT_NODE)
					&& ("bf:param".equals(child.getNodeName()))) {
				Element element = (Element) child;

				if ("bf:playerstat".equals(element.getNodeName())) {
					String playerId = element.getAttribute("playerid");
					String name = getRoundStatsParameter(element, "name");

					if ((playerId != null) && (name != null)) {
						Alias player = new Alias();
						player.setId(playerId);
						player.setName(name);
						playerManager.providePlayerRecord(player);

						String scoreString = getRoundStatsParameter(element,
								"score");

						if (scoreString != null) {
							float scoreValue = Float.parseFloat(scoreString);

							Score score = new Score();
							score.setAlias(player);
							score.setScore(scoreValue);

							scoreManager.provideScoreRecord(score);
						}
					}
				}
			}
		}
	}

}
