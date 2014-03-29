package jepperscore.backends.activemq;

import java.io.StringReader;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import jepperscore.dao.AbstractMessageSource;
import jepperscore.dao.IMessageSource;
import jepperscore.dao.transport.TransportMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the {@link IMessageSource} using ActiveMQ.
 * 
 * @author Chuck
 * 
 */
public class ActiveMQMessageSource extends AbstractMessageSource {

	/**
	 * Class logger.
	 */
	private static final Logger LOG = LoggerFactory
			.getLogger(ActiveMQMessageSource.class);

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
	 * The message consumer.
	 */
	private MessageConsumer consumer;

	/**
	 * Creates the message destination.
	 * 
	 * @param activeMqConnection
	 *            The connection string to use for ActiveMQ.
	 * @throws JMSException
	 *             If there is a problem setting up ActiveMQ.
	 * @throws JAXBException
	 *             If there is a problem setting up the deserializer.
	 */
	public ActiveMQMessageSource(String activeMqConnection)
			throws JMSException, JAXBException {
		ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(
				activeMqConnection);
		conn = cf.createConnection();
		conn.start();

		session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
		eventTopic = session.createTopic(ActiveMQBackendConstants.EVENT_TOPIC);
		consumer = session.createConsumer(eventTopic);

		jaxbContext = JAXBContext.newInstance(TransportMessage.class);
		final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

		consumer.setMessageListener(new MessageListener() {

			@Override
			public void onMessage(Message message) {
				try {
					if (message instanceof TextMessage) {
						TextMessage txtMessage = (TextMessage) message;

						try (StringReader reader = new StringReader(txtMessage
								.getText())) {
							call((TransportMessage) unmarshaller
									.unmarshal(reader));
						} catch (JMSException e) {
							LOG.error(e.getMessage(), e);
						}
					} else {
						LOG.warn("Got message of unknown type: "
								+ message.getClass().getSimpleName());
					}
				} catch (JAXBException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		});
	}
}
