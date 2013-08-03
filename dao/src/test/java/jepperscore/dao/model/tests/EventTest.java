package jepperscore.dao.model.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import jepperscore.dao.model.Event;

import org.joda.time.DateTime;
import org.junit.Test;

/**
 * This tests the {@link Event} class.
 * @author Chuck
 *
 */
public class EventTest {

	/**
	 * Sample date used for testing.
	 */
	public static final DateTime dt = new DateTime(2012, 01, 03, 10, 05, 00);

	/**
	 * Test XML for simple {@link Event}.
	 */
	public static final String TEST_SIMPLE_EVENT = "<event timestamp='"
			+ dt.toString() + "'/>";

	/**
	 * Tests the unmarshalling of a simple {@link Event}.
	 * @throws JAXBException
	 */
	@Test
	public void testSimpleUnmarshalling() throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(Event.class);
		Unmarshaller unmarshaller = jc.createUnmarshaller();

		StringReader reader = new StringReader(TEST_SIMPLE_EVENT);
		Event e = (Event) unmarshaller.unmarshal(reader);

		assertNotNull(e);
		assertEquals(dt, e.getTimestamp());
	}

}
