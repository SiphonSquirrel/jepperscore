package jepperscore.jepperconsole;

import java.io.StringReader;

import javax.annotation.Nonnull;
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
import jepperscore.dao.model.Team;
import jepperscore.dao.transport.TransportMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the main method for the JepperConsole app.
 *
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
	 *
	 * @param args
	 *            [Active MQ Connection String]
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			throw new RuntimeException(
					"Incorrect arguments! Need [Active MQ Connection String]");
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
			throw new RuntimeException(e);
		}

		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Do nothing.
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
			throw new RuntimeException(e);
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
					Alias alias = transportMessage.getAlias();
					Event event = transportMessage.getEvent();
					Round round = transportMessage.getRound();
					Score score = transportMessage.getScore();
					ServerMetadata metadata = transportMessage
							.getServerMetadata();
					Team team = transportMessage.getTeam();

					if (alias != null) {
						handleAlias(alias);
					} else if (event != null) {
						handleEvent(event);
					} else if (round != null) {
						handleRound(round);
					} else if (score != null) {
						handleScore(score);
					} else if (metadata != null) {
						handleMetadata(metadata);
					} else if (team != null) {
						handleTeam(team);
					} else {
						Object content = transportMessage.getMessageContent();
						if (content == null) {
							LOG.warn("Not sure what was transported, content was null");
						} else {
							LOG.warn("Not sure what was transported: "
									+ content.getClass().getSimpleName());
						}
					}
				}
			} catch (JAXBException | JMSException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * This function handles server metadata messages.
	 *
	 * @param serverMetadata
	 *            The server metadata.
	 */
	private void handleMetadata(@Nonnull ServerMetadata serverMetadata) {

	}

	/**
	 * This function handles score messages.
	 *
	 * @param score
	 *            The score.
	 */
	private void handleScore(@Nonnull Score score) {

	}

	/**
	 * This function handles round messages.
	 *
	 * @param round
	 *            The round.
	 */
	private void handleRound(@Nonnull Round round) {

	}

	/**
	 * This function handles event messages.
	 *
	 * @param event
	 *            The event.
	 */
	private void handleEvent(@Nonnull Event event) {
		String text = event.getParsedEventText();
		if (!text.isEmpty()) {
			LOG.info(text);
		}
	}

	/**
	 * This function handles alias messages.
	 *
	 * @param alias
	 *            The alias.
	 */
	private void handleAlias(@Nonnull Alias alias) {
		StringBuffer msg = new StringBuffer();
		msg.append("Got update for alias: " + alias.getName() + " ("
				+ alias.getId() + ")");
		Team team = alias.getTeam();
		if (team != null) {
			msg.append(" on team " + team.getTeamName());
		}

		LOG.info(msg.toString());
	}

	/**
	 * This function handles team messages.
	 *
	 * @param team
	 *            The team.
	 */
	private void handleTeam(@Nonnull Team team) {

	}

}
