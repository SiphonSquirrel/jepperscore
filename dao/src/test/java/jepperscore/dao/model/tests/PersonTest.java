package jepperscore.dao.model.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import jepperscore.dao.model.Person;

import org.junit.Test;

public class PersonTest {

	public static final String TEST_SIMPLE_PERSON = "<person name='TestPersonName'/>";

	@Test
	public void test() throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(Person.class);
		Unmarshaller unmarshaller = jc.createUnmarshaller();

		StringReader reader = new StringReader(TEST_SIMPLE_PERSON);
		Person p = (Person) unmarshaller.unmarshal(reader);

		assertNotNull(p);
		assertEquals("TestPersonName", p.getName());
	}

}
