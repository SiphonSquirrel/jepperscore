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
	 * This is the {@link JAXBContext} for use with the {@link Messages} class.
	 */
	private final JAXBContext jaxbContext;

	/**
	 * This is the default constructor. It initializes a {@link JAXBContext} for
	 * unmarshalling the {@link Messages} class.
	 * 
	 * @throws JAXBException
	 */
	public PersistenceListener() throws JAXBException {
		jaxbContext = JAXBContext.newInstance(TransportMessage.class);
	}

	@Override
	public void onMessage(Message message) {
		if (message == null)
			return;
		
		if (message instanceof TextMessage) {
			TextMessage textMessage = (TextMessage) message;

			try {
				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

				TransportMessage transportMessage = (TransportMessage) unmarshaller
						.unmarshal(new StringReader(textMessage.getText()));

				if (transportMessage.getEvent() != null) {
					onEvent(transportMessage.getEvent());
				}
			} catch (JAXBException | JMSException e) {
				LOG.error(e.getMessage(), e);
			}

		}
	}

	/**
	 * This function is called when the persistence listener receives an event.
	 * 
	 * @param e
	 */
	protected void onEvent(@Nonnull Event e) {

	}

}
