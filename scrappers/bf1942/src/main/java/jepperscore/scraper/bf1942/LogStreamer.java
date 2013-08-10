package jepperscore.scraper.bf1942;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import jepperscore.dao.model.Alias;
import jepperscore.dao.model.Event;
import jepperscore.dao.model.EventCode;
import jepperscore.dao.model.Score;
import jepperscore.dao.transport.TransportMessage;

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
	 * The JAXB Context for marshalling {@link TransportMessage}.
	 */
	private JAXBContext jaxbContext;

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
	 * This holds the UUID for the round.
	 */
	private String sessionUUID = "";

	/**
	 * Players discovered so far this match.
	 */
	private Map<String, Alias> players = new HashMap<String, Alias>();

	/**
	 * This constructor points the log streamer at a log file.
	 * 
	 * @param stream
	 *            The log stream to watch.
	 * @param session The ActiveMQ {@link Session} to use.
	 * @param producer The ActiveMQ {@link MessageProducer} to use.
	 */
	public LogStreamer(@Nonnull InputStream stream, @Nonnull Session session,
			@Nonnull MessageProducer producer) {
		this.stream = stream;
		this.session = session;
		this.producer = producer;
		this.charset = Charset.forName("iso-8859-1");

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			dbBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			LOG.error(e.getMessage(), e);
		}

		try {
			this.jaxbContext = JAXBContext.newInstance(TransportMessage.class);
		} catch (JAXBException e) {
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
					players.clear();
					sessionUUID = UUID.randomUUID().toString();
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
	 * Searches the player map for the passed id.
	 * 
	 * @param id
	 *            The id to search for.
	 * @return The player.
	 */
	@Nonnull
	private Alias getPlayer(@Nonnull String id) {
		Alias retVal = players.get(id);

		if (retVal == null) {
			retVal = new Alias();
			retVal.setId(sessionUUID + "-bot-" + id);
		}

		return retVal;
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
					return parameter.getNodeValue();
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
	 * Parses the special characters out of the name.
	 * 
	 * @param name
	 *            The unparsed name.
	 * @return The parsed name.
	 */
	private String parseName(String name) {
		return name;
	}
	
	/**
	 * This function sends a message to ActiveMQ. 
	 * @param transportMessage The message to send.
	 */
	private void sendMessage(TransportMessage transportMessage) {
		try {
			Marshaller marshaller = jaxbContext.createMarshaller();

			StringWriter writer = new StringWriter();
			marshaller.marshal(transportMessage, writer);

			producer.send(session.createTextMessage(writer.toString()));
		} catch (JMSException | JAXBException e) {
			LOG.error(e.getMessage(), e);
		}
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
		case "scoreEvent":
			newEvent = new Event();

			String attackerId = getEventParameter(eventElement, "player_id");
			String victimId = getEventParameter(eventElement, "victim_id");
			String scoreType = getEventParameter(eventElement, "score_type");

			if (scoreType == null) {
				break;
			}

			if (attackerId != null) {
				newEvent.setAttacker(getPlayer(attackerId));
			}

			if (victimId != null) {
				newEvent.setAttacker(getPlayer(victimId));
			}
			switch (scoreType) {
			case "Kill":
				String weapon = getEventParameter(eventElement, "weapon");
				if ("(none)".equals(weapon)) {
					weapon = "killed";
				}

				newEvent.setEventText(String.format("%s [%s] %s"));
				eventCode.setObject(weapon);
				newEvent.setEventCode(eventCode);
				break;
			case "DeathNoMsg":
				// Not important, ignore.
				break;
			}

			break;

		case "roundInit":
		case "spawnEvent":
		case "pickupKit":
		case "beginMedPack":
		case "endMedPack":
		case "enterVehicle":
		case "destroyVehicle":
			break;

		default:
			LOG.info("Unhandled event type: " + eventType);
		}

		if (newEvent != null) {
			TransportMessage transportMessage = new TransportMessage();
			transportMessage.setEvent(newEvent);

			sendMessage(transportMessage);
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
					Alias player = getPlayer(playerId);
					player.setName(parseName(getRoundStatsParameter(element,
							"name")));

					String scoreString = getRoundStatsParameter(element,
							"score");

					if (scoreString != null) {
						float scoreValue = Float.parseFloat(scoreString);

						Score score = new Score();
						score.setAlias(player);
						score.setScore(scoreValue);

						TransportMessage transportMessage = new TransportMessage();
						transportMessage.setScore(score);

						sendMessage(transportMessage);
					}
				}
			}
		}
	}

}
