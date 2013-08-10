package jepperscore.scraper.common.query;

/**
 * This interface defines a listener for query client. 
 * @author Chuck
 *
 */
public interface QueryClientListener {

	/**
	 * Called by the query client when new query information is retrieved.
	 * @param info The information retrieved.
	 */
	void queryClient(QueryCallbackInfo info);
}
