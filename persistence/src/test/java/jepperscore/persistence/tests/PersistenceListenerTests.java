package jepperscore.persistence.tests;

import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import jepperscore.dao.DaoConstant;
import jepperscore.dao.model.Event;
import jepperscore.dao.transport.Messages;
import jepperscore.persistence.PersistenceListener;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * This class tests the {@link PersistenceListener} class.
 * 
 * @author Chuck
 * 
 */
public class PersistenceListenerTests {

	/**
	 * This subclass of {@link PersistenceListener} does nothing with the
	 * messages it receives, but saves them for later consumption.
	 * 
	 * @author Chuck
	 * 
	 */
	private static class TestablePersistenceListener extends
			PersistenceListener {
		/**
		 * This list saves the encountered events.
		 */
		private List<Event> events = new CopyOnWriteArrayList<Event>();

		/**
		 * This constructor sets up the {@link PersistenceListener} for testing.
		 * @throws JAXBException Thrown from the super class.
		 */
		public TestablePersistenceListener() throws JAXBException {
			super();
		}

		@Override
		protected void onEvent(Event e) {
			synchronized (getEvents()) {
				getEvents().add(e);
			}
		}

		/**
		 * @return The events
		 */
		public List<Event> getEvents() {
			return events;
		}
	}

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
	 * The current ActiveMQ event topic.
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
	 * This test ensures that messages are unmarshalled correctly.
	 * 
	 * @throws Exception
	 *             If something goes wrong in the test.
	 */
	@Test
	public void testEventUnmarshalling() throws Exception {
		MessageProducer producer = session.createProducer(eventTopic);
		MessageConsumer consumer = session.createConsumer(eventTopic);

		TestablePersistenceListener listener = new TestablePersistenceListener();
		consumer.setMessageListener(listener);

		Messages collection = new Messages();
		Event event1 = new Event();
		collection.getEvent().add(event1);

		JAXBContext jaxbContext = JAXBContext.newInstance(Messages.class);
		Marshaller marshaller = jaxbContext.createMarshaller();

		StringWriter writer = new StringWriter();
		marshaller.marshal(collection, writer);

		producer.send(session.createTextMessage(writer.toString()));

		Thread.sleep(100);

		Assert.assertEquals(1, listener.getEvents().size());
	}

}
