package jepperscore.backends.testing;

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
	 * Creates the message destination.
	 *
	 * @param emptySetupString
	 *            Does nothing yet.
	 */
	public NullMessageDestination(String emptySetupString) {
	}

	@Override
	public synchronized void sendMessage(TransportMessage transportMessage) {

	}

}
