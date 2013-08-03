package jepperscore.dao.transport.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import jepperscore.dao.model.Event;
import jepperscore.dao.model.tests.EventTest;
import jepperscore.dao.transport.TransportMessage;

import org.junit.Test;

/**
 * This class tests the {@link TransportMessage} class.
 * @author Chuck
 *
 */
public class TransportMessageTest {

	/**
	 * Example XML for {@link TransportMessage} with an {@link Event}.
	 */
	public static final String TEST_EVENT = "<message>"
			+ EventTest.TEST_SIMPLE_EVENT + "</message>";

	/**
	 * Tests XML input when an event.
	 * @throws JAXBException When something goes awry.
	 */
	@Test
	public void testEventMessage() throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(TransportMessage.class);
		Unmarshaller unmarshaller = jc.createUnmarshaller();

		StringReader reader = new StringReader(TEST_EVENT);
		TransportMessage msg = (TransportMessage) unmarshaller
				.unmarshal(reader);

		assertNotNull(msg);
		assertNotNull(msg.getEvent());
		assertNull(msg.getRound());
		assertEquals(msg.getEvent(), msg.getMessageContent());
	}

}
