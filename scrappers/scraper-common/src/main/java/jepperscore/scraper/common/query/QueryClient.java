package jepperscore.scraper.common.query;

/**
 * This interface defines the common query client functions.
 * @author Chuck
 *
 */
public interface QueryClient {

	/**
	 * Starts the query client.
	 */
	void start();

	/**
	 * Stops the query client.
	 */
	void stop();
	
	/**
	 * Registers the listener.
	 * @param listener The listener to register.
	 */
	void registerListener(QueryClientListener listener);
	
	/**
	 * Unregisters the listener.
	 * @param listener The listener to unregister.
	 */
	void unregisterListener(QueryClientListener listener);
}
