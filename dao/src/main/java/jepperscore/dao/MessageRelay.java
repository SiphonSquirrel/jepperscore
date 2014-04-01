package jepperscore.dao;

import jepperscore.dao.transport.TransportMessage;

/**
 * This class takes a source and a destination and relays the message from the source to the destination.
 * @author Chuck
 *
 */
public class MessageRelay implements IMessageCallback {
	/**
	 * The message source.
	 */
	private AbstractMessageSource source;

	/**
	 * The message destination.
	 */
	private IMessageDestination dest;

	/**
	 * Constructor.
	 * @param source The message source.
	 * @param dest The message destination.
	 */
	public MessageRelay(AbstractMessageSource source, IMessageDestination dest) {
		this.source = source;
		this.dest = dest;

		this.source.registerCallback(this);
	}

	@Override
	public void onMessage(TransportMessage message) {
		dest.sendMessage(message);
	}
}
