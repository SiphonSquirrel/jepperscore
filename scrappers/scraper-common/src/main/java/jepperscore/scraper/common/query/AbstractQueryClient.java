package jepperscore.scraper.common.query;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class works as a base class for query clients.
 *
 * @author Chuck
 *
 */
public abstract class AbstractQueryClient implements QueryClient, Runnable {

	/**
	 * This is a collection of registered listeners.
	 */
	private Map<String, QueryClientListener> listeners = new ConcurrentHashMap<String, QueryClientListener>();

	/**
	 * This is used to keep track of the thread that was started by this class.
	 */
	private volatile Thread thread = null;

	/**
	 * How long, in milliseconds to wait until querying again.
	 */
	private int period = 250;

	@Override
	public synchronized void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.setDaemon(true);
			thread.start();
		}
	}

	@Override
	public synchronized void stop() {
		if (thread != null) {
			thread = null;
		}
	}

	@Override
	public synchronized void registerListener(String queryType,
			QueryClientListener listener) {
		listeners.put(queryType, listener);
	}

	@Override
	public synchronized void unregisterListener(String queryType) {
		listeners.remove(queryType);
	}

	/**
	 * Sets the query period.
	 *
	 * @param period
	 *            The time in milliseconds between query calls.
	 */
	public void setQueryPeriod(int period) {
		this.period = period;
	}

	/**
	 * Queries the game server.
	 *
	 * @param queryType
	 *            The type of query to do.
	 */
	protected abstract void query(String queryType);

	/**
	 * Makes callbacks to all registered {@link QueryClientListener}.
	 *
	 * @param queryType
	 *            The type of query.
	 * @param info
	 *            The info sent to all registered listeners.
	 */
	protected synchronized void makeCallbacks(String queryType,
			QueryCallbackInfo info) {
		QueryClientListener listener = listeners.get(queryType);
		if (listener != null) {
			listener.queryClient(info);
		}
	}

	/**
	 * This method handles the query polling.
	 */
	@Override
	public void run() {
		while (thread == Thread.currentThread()) {

			for (String queryType : listeners.keySet()) {
				query(queryType);
			}
			try {
				Thread.sleep(period);
			} catch (InterruptedException e) {
				thread = null;
				break;
			}
		}
	}

}
