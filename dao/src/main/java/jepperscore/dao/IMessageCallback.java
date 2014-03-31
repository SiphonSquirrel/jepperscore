package jepperscore.dao;

import javax.annotation.Nonnull;

import jepperscore.dao.transport.TransportMessage;

/**
 * This interface provides a callback method for dealing with transport
 * messages.
 *
 * @author Chuck
 */
public interface IMessageCallback {

	/**
	 * Called when there is a {@link TransportMessage} event.
	 * @param message The message.
	 */
	void onMessage(@Nonnull final TransportMessage message);

}
