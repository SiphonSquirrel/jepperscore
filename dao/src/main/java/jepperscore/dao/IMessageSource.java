package jepperscore.dao;

/**
 * This class represents a source backend for messages.
 * @author Chuck
 */
public interface IMessageSource {

	/**
	 * Registers a callback with the message source.
	 * @param callback The callback.
	 */
	void registerCallback(IMessageCallback callback);

	/**
	 * Unregisters a callback with the message source.
	 * @param callback The callback.
	 */
	void unregisterCallback(IMessageCallback callback);

}
