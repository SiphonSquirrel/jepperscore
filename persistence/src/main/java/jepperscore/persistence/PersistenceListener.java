package jepperscore.persistence;

import java.io.StringReader;

import javax.annotation.Nonnull;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import jepperscore.dao.model.Event;
import jepperscore.dao.model.Round;
import jepperscore.dao.transport.TransportMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the {@link MessageListener} interface for ActiveMQ to
 * provide an endpoint to save messages to a database.
 *
 * @author Chuck
 *
 */
public class PersistenceListener implements MessageListener {
	/**
	 * This is the logger for this class.
	 */
	private static final Logger LOG = LoggerFactory
			.getLogger(PersistenceListener.class);

	/**
	 * This is the {@link JAXBContext} for use with the {@link Message} class.
	 */
	private final JAXBContext jaxbContext;

	/**
	 * This is the default constructor. It initializes a {@link JAXBContext} for
	 * unmarshalling the {@link Message} class.
	 *
	 * @throws JAXBException When there is a problem creating an instance of the {@link JAXBContext}.
	 */
	public PersistenceListener() throws JAXBException {
		jaxbContext = JAXBContext.newInstance(TransportMessage.class);
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
					Event event = transportMessage.getEvent();
					Round round = transportMessage.getRound();

					if (event != null) {
						onEvent(event);
					}
					else if (round != null) {
						onRound(round);
					}
					else {
						LOG.warn("Unable to determine message contents.");
					}
				} else {
					LOG.warn("Unable to unmarshall TransportMessage.");
				}
			} catch (JAXBException | JMSException e) {
				LOG.error(e.getMessage(), e);
			}

		}
	}

	/**
	 * This function is called when the persistence listener receives an event.
	 *
	 * @param e The event.
	 */
	protected void onEvent(@Nonnull Event e) {

	}

	/**
	 * This function is called when the persistence listener receives a round.
	 *
	 * @param r The round.
	 */
	protected void onRound(@Nonnull Round r) {

	}

}
