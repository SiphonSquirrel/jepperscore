package jepperscore.backends.testing;

import java.util.Collection;
import java.util.LinkedList;

import javax.annotation.Nonnull;

import jepperscore.dao.IMessageDestination;
import jepperscore.dao.transport.TransportMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the {@link IMessageDestination} using ActiveMQ.
 *
 * @author Chuck
 *
 */
public class TestingMessageDestination implements IMessageDestination {

	/**
	 * Used as a simple callback for testing.
	 * @author Chuck
	 *
	 */
	public static interface MessageHandler {
		/**
		 * Called when a new message arrives.
		 * @param message The message.
		 */
		void onMessage(TransportMessage message);
	}

	/**
	 * Class logger.
	 */
	private static final Logger LOG = LoggerFactory
			.getLogger(TestingMessageDestination.class);

	/**
	 * The messages.
	 */
	private LinkedList<TransportMessage> messages = new LinkedList<TransportMessage>();

	/**
	 * The message handlers.
	 */
	private LinkedList<MessageHandler> handlers = new LinkedList<MessageHandler>();

	/**
	 * Creates the message destination.
	 *
	 * @param emptySetupString
	 *            Does nothing yet.
	 */
	public TestingMessageDestination(String emptySetupString) {
	}

	@Override
	public synchronized void sendMessage(TransportMessage transportMessage) {
		LOG.trace(transportMessage.toString());
		messages.add(transportMessage);
		for (MessageHandler handler: handlers) {
			handler.onMessage(transportMessage);
		}
	}

	/**
	 * @return All them messages seen so far.
	 */
	public synchronized Collection<TransportMessage> getMessages() {
		return new LinkedList<TransportMessage>(messages);
	}

	/**
	 * Clears all the messages.
	 */
	public synchronized void clearMessages() {
		messages.clear();
	}

	/**
	 * @return The number of messages queued.
	 */
	public synchronized int count() {
		return messages.size();
	}

	/**
	 * Adds a message handler.
	 * @param handler The handler to add.
	 */
	public synchronized void addHandler(@Nonnull MessageHandler handler) {
		handlers.add(handler);
	}

}
