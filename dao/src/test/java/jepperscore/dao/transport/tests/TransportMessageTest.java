package jepperscore.dao.transport.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import jepperscore.dao.model.tests.AliasTest;
import jepperscore.dao.model.tests.EventTest;
import jepperscore.dao.transport.TransportMessage;

import org.junit.Test;

public class TransportMessageTest {

	public static final String TEST_ALIAS = "<message>"
			+ AliasTest.TEST_SIMPLE_ALIAS + "</message>";
	public static final String TEST_EVENT = "<message>"
			+ EventTest.TEST_SIMPLE_EVENT + "</message>";

	@Test
	public void testAliasMessage() throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(TransportMessage.class);
		Unmarshaller unmarshaller = jc.createUnmarshaller();

		StringReader reader = new StringReader(TEST_ALIAS);
		TransportMessage msg = (TransportMessage) unmarshaller
				.unmarshal(reader);

		assertNotNull(msg);
		assertNotNull(msg.getAlias());
		assertNull(msg.getEvent());
		assertEquals(msg.getAlias(), msg.getMessageContent());
	}

	@Test
	public void testEventMessage() throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(TransportMessage.class);
		Unmarshaller unmarshaller = jc.createUnmarshaller();

		StringReader reader = new StringReader(TEST_EVENT);
		TransportMessage msg = (TransportMessage) unmarshaller
				.unmarshal(reader);

		assertNotNull(msg);
		assertNull(msg.getAlias());
		assertNotNull(msg.getEvent());
		assertEquals(msg.getEvent(), msg.getMessageContent());
	}

}
