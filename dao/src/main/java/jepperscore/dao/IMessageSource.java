package jepperscore.dao;

import javax.annotation.Nonnull;

/**
 * This class represents a source backend for messages.
 * @author Chuck
 */
public interface IMessageSource {

	/**
	 * Registers a callback with the message source.
	 * @param callback The callback.
	 */
	void registerCallback(@Nonnull IMessageCallback callback);

	/**
	 * Unregisters a callback with the message source.
	 * @param callback The callback.
	 */
	void unregisterCallback(@Nonnull IMessageCallback callback);

}
