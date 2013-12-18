package jepperscore.dao.transport.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import jepperscore.dao.model.Event;
import jepperscore.dao.model.Team;
import jepperscore.dao.model.tests.EventTest;
import jepperscore.dao.transport.TransportMessage;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class tests the {@link TransportMessage} class.
 *
 * @author Chuck
 *
 */
public class TransportMessageTest {

	/**
	 * Example XML for {@link TransportMessage} with an {@link Event}.
	 */
	public static final String TEST_XML_EVENT = "<message>"
			+ EventTest.TEST_SIMPLE_EVENT + "</message>";

	/**
	 * Example JSON for {@link TransportMessage} with an {@link Team}.
	 */
	public static final String TEST_JSON_TEAM = "{\"_id\":\"test-id\",\"_rev\":\"test-rev\",\"team\":{\"teamName\":\"Test Team\",\"score\":1.0}}";

	/**
	 * Tests XML input when an event.
	 *
	 * @throws JAXBException
	 *             When something goes awry.
	 */
	@Test
	public void testXmlEventMessage() throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(TransportMessage.class);
		Unmarshaller unmarshaller = jc.createUnmarshaller();

		StringReader reader = new StringReader(TEST_XML_EVENT);
		TransportMessage msg = (TransportMessage) unmarshaller
				.unmarshal(reader);

		assertNotNull(msg);
		assertNotNull(msg.getEvent());
		assertNull(msg.getRound());
		assertEquals(msg.getEvent(), msg.getMessageContent());
	}

	/**
	 * Tests JSON input when a team.
	 *
	 * @throws IOException
	 *             When something goes awry.
	 * @throws JsonMappingException
	 *             When something goes awry.
	 * @throws JsonParseException
	 *             When something goes awry.
	 */
	@Test
	public void testJsonTeamMessage() throws JsonParseException,
			JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();

		TransportMessage msg = mapper.readValue(TEST_JSON_TEAM,
				TransportMessage.class);

		assertNotNull(msg);
		assertNull(msg.getAlias());
		assertNull(msg.getEvent());
		assertNull(msg.getRound());
		assertNull(msg.getScore());
		assertNull(msg.getServerMetadata());
		assertNotNull(msg.getTeam());
		assertEquals(msg.getTeam(), msg.getMessageContent());
	}
}
