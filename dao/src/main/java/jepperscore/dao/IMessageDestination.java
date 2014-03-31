package jepperscore.dao;

import javax.annotation.Nonnull;

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
	void sendMessage(@Nonnull final TransportMessage transportMessage);
}
