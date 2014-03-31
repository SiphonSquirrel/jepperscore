package jepperscore.backends.activemq;

import java.io.StringWriter;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import jepperscore.dao.IMessageDestination;
import jepperscore.dao.transport.TransportMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the {@link IMessageDestination} using ActiveMQ.
 * @author Chuck
 *
 */
public class ActiveMQMessageDestination implements IMessageDestination {

	/**
	 * Class logger.
	 */
	private static final Logger LOG = LoggerFactory
			.getLogger(ActiveMQMessageDestination.class);

	/**
	 * The JAXB Context.
	 */
	private final JAXBContext jaxbContext;

	/**
	 * The ActiveMQ connection.
	 */
	private final Connection conn;

	/**
	 * The ActiveMQ session.
	 */
	private final Session session;

	/**
	 * The ActiveMQ topic.
	 */
	private final Topic eventTopic;

	/**
	 * The ActiveMQ producer.
	 */
	private final MessageProducer producer;

	/**
	 * Creates the message destination.
	 * @param activeMqConnection The connection string to use for ActiveMQ.
	 * @throws JMSException If there is a problem setting up ActiveMQ.
	 * @throws JAXBException If there is a problem setting up the deserializer.
	 */
	public ActiveMQMessageDestination(String activeMqConnection)
			throws JMSException, JAXBException {
		ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(
				activeMqConnection);
		conn = cf.createConnection();
		conn.start();

		session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
		eventTopic = session.createTopic(ActiveMQBackendConstants.EVENT_TOPIC);
		producer = session.createProducer(eventTopic);

		jaxbContext = JAXBContext.newInstance(TransportMessage.class);

	}

	@Override
	public synchronized void sendMessage(TransportMessage transportMessage) {
		if (transportMessage.getSessionId() == null) {
			LOG.warn("Sending message without session ID.");
		}
		
		try {
			Marshaller marshaller = jaxbContext.createMarshaller();

			StringWriter writer = new StringWriter();
			marshaller.marshal(transportMessage, writer);

			producer.send(session.createTextMessage(writer.toString()));
		} catch (JMSException | JAXBException e) {
			LOG.error(e.getMessage(), e);
		}
	}

}
