package jepperscore.dao.model.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import jepperscore.dao.model.Alias;

import org.junit.Test;

/**
 * @author Chuck
 * 
 */
public class AliasTest {

	public static final String TEST_SIMPLE_ALIAS = "<alias name='TestAliasName'/>";
	public static final String TEST_COMPLEX_ALIAS = "<alias name='TestAliasName'>" + PersonTest.TEST_SIMPLE_PERSON + "</alias>";

	@Test
	public void testSimpleUnmarshalling() throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(Alias.class);
		Unmarshaller unmarshaller = jc.createUnmarshaller();

		StringReader reader = new StringReader(TEST_SIMPLE_ALIAS);
		Alias a = (Alias) unmarshaller.unmarshal(reader);

		assertNotNull(a);
		assertEquals("TestAliasName", a.getName());
		assertNull(a.getPerson());
	}

	@Test
	public void testComplexUnmarshalling() throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(Alias.class);
		Unmarshaller unmarshaller = jc.createUnmarshaller();

		StringReader reader = new StringReader(TEST_COMPLEX_ALIAS);
		Alias a = (Alias) unmarshaller.unmarshal(reader);

		assertNotNull(a);
		assertEquals("TestAliasName", a.getName());
		assertNotNull(a.getPerson());
		assertEquals("TestPersonName", a.getPerson().getName());
	}
}
