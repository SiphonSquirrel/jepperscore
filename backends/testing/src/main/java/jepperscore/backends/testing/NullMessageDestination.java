package jepperscore.backends.testing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jepperscore.dao.IMessageDestination;
import jepperscore.dao.transport.TransportMessage;

/**
 * This class implements the {@link IMessageDestination} using ActiveMQ.
 *
 * @author Chuck
 *
 */
public class NullMessageDestination implements IMessageDestination {

	/**
	 * Class logger.
	 */
	private static final Logger LOG = LoggerFactory
			.getLogger(NullMessageDestination.class);
	
	/**
	 * Creates the message destination.
	 *
	 * @param emptySetupString
	 *            Does nothing yet.
	 */
	public NullMessageDestination(String emptySetupString) {
	}

	@Override
	public synchronized void sendMessage(TransportMessage transportMessage) {
		if (transportMessage.getSessionId() == null) {
			LOG.warn("Sending message without session ID.");
		}
	}

}
