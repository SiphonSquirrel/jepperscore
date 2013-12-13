package jepperscore.dao;

import jepperscore.dao.transport.TransportMessage;

/**
 * This class represents a destination backend for messages.
 * @author Chuck
 *
 */
public interface IMessageDestination {
	/**
	 * Sends a message to the backend.
	 * @param transportMessage
	 */
	void sendMessage(final TransportMessage transportMessage);
}
