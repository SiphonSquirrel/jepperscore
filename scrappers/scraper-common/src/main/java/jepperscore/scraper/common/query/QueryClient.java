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
	 * @param queryType The type of query to do.
	 * @param listener The listener to register.
	 */
	void registerListener(String queryType, QueryClientListener listener);

	/**
	 * Unregisters the listener.
	 * @param queryType The type of query to do.
	 */
	void unregisterListener(String queryType);
}
