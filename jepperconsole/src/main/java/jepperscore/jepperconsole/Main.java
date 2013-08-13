package jepperscore.jepperconsole;

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

import jepperscore.dao.DaoConstant;
import jepperscore.dao.model.Alias;
import jepperscore.dao.model.Event;
import jepperscore.dao.model.Round;
import jepperscore.dao.model.Score;
import jepperscore.dao.model.ServerMetadata;
import jepperscore.dao.transport.TransportMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the main method for the JepperConsole app.
 * @author Chuck
 *
 */
public class Main implements MessageListener {

	/**
	 * The logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	/**
	 * The JAXB context to use.
	 */
	private JAXBContext jaxbContext;

	/**
	 * The main function.
	 * @param args [Active MQ Connection String]
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err
					.println("Incorrect arguments! Need [Active MQ Connection String]");
			System.exit(1);
		}
		String activeMqConnection = args[0];

		ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(
				activeMqConnection);

		Connection conn;
		Session session;
		Topic eventTopic;

		try {
			conn = cf.createConnection();
			conn.start();

			session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
			eventTopic = session.createTopic(DaoConstant.EVENT_TOPIC);

			MessageConsumer consumer = session.createConsumer(eventTopic);

			consumer.setMessageListener(new Main());
		} catch (JMSException e) {
			LOG.error(e.getMessage(), e);
			System.exit(2);
			return;
		}

		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				//Do nothing.
			}
		}
	}

	/**
	 * Default constructor. Sets up JAXB Context.
	 */
	public Main() {
		try {
			jaxbContext = JAXBContext.newInstance(TransportMessage.class);
		} catch (JAXBException e) {
			LOG.error(e.getMessage(), e);
			System.exit(1);
			return;
		}
	}

	@Override
	public void onMessage(Message message) {
		if (message == null) {
			return;
		}

		if (message instanceof TextMessage) {
			TextMessage textMessage = (TextMessage) message;

			try {
				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

				TransportMessage transportMessage = (TransportMessage) unmarshaller
						.unmarshal(new StringReader(textMessage.getText()));

				if (transportMessage != null) {
					if (transportMessage.getAlias() != null) {
						handleAlias(transportMessage.getAlias());
					} else if (transportMessage.getEvent() != null) {
						handleEvent(transportMessage.getEvent());
					} else if (transportMessage.getRound() != null) {
						handleRound(transportMessage.getRound());
					} else if (transportMessage.getScore() != null) {
						handleScore(transportMessage.getScore());
					} else if (transportMessage.getServerMetadata() != null) {
						handleMetadata(transportMessage.getServerMetadata());
					} else {
						LOG.warn("Not sure what was transported...");
					}
				}
			} catch (JAXBException | JMSException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * This function handles server metadata messages.
	 * @param serverMetadata The server metadata.
	 */
	private void handleMetadata(ServerMetadata serverMetadata) {

	}

	/**
	 * This function handles score messages.
	 * @param score The score.
	 */
	private void handleScore(Score score) {

	}

	/**
	 * This function handles round messages.
	 * @param round The round.
	 */
	private void handleRound(Round round) {

	}

	/**
	 * This function handles event messages.
	 * @param event The event.
	 */
	private void handleEvent(Event event) {
		String text = event.getParsedEventText();
		if (!text.isEmpty()) {
			LOG.info(text);
		}
	}

	/**
	 * This function handles alias messages.
	 * @param alias The alias.
	 */
	private void handleAlias(Alias alias) {

	}

}
