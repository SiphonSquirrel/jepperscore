package jepperscore.dao;

import java.util.LinkedList;
import java.util.List;

import jepperscore.dao.IMessageCallback;
import jepperscore.dao.IMessageSource;
import jepperscore.dao.transport.TransportMessage;

/**
 * This works as a base class for implementing message sources.
 * @author Chuck
 *
 */
public abstract class AbstractMessageSource implements IMessageSource {

	/**
	 * The list of callbacks.
	 */
	private List<IMessageCallback> callbacks = new LinkedList<IMessageCallback>();
	
	@Override
	public void registerCallback(IMessageCallback callback) {
		synchronized (callbacks) {
			callbacks.add(callback);
		}
	}

	@Override
	public void unregisterCallback(IMessageCallback callback) {
		synchronized (callbacks) {
			callbacks.remove(callback);
		}
	}

	/**
	 * Sends a message to the registered call backs.
	 * @param transportMessage
	 */
	protected void call(TransportMessage transportMessage) {
		synchronized (callbacks) {
			for (IMessageCallback callback : callbacks) {
				callback.onMessage(transportMessage);
			}
		}
	}
}
