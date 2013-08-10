package jepperscore.scraper.common.query;

import java.util.HashSet;
import java.util.Set;

/**
 * This class works as a base class for query clients.
 * @author Chuck
 *
 */
public abstract class AbstractQueryClient implements QueryClient, Runnable {

	/**
	 * This is a collection of registered listeners.
	 */
	private Set<QueryClientListener> listeners = new HashSet<QueryClientListener>();
	
	/**
	 * This is used to keep track of the thread that was started by this class.
	 */
	private Thread thread = null;
	
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
	public synchronized void registerListener(QueryClientListener listener) {
		listeners.add(listener);
	}
	
	@Override
	public synchronized void unregisterListener(QueryClientListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Sets the query period.
	 * @param period The time in milliseconds between query calls.
	 */
	public void setQueryPeriod(int period) {
		this.period = period;
	}
	
	/**
	 * Queries the game server.
	 */
	protected abstract void query();
	
	/**
	 * Makes callbacks to all registered {@link QueryClientListener}.
	 * @param info The info sent to all registered listeners.
	 */
	protected synchronized void makeCallbacks(QueryCallbackInfo info) {
		for (QueryClientListener listener : listeners) {
			listener.queryClient(info);
		}
	}
	
	/**
	 * This method handles the query polling.
	 */
	@Override
	public void run() {
		while (thread == Thread.currentThread()) {
			query();
			try {
				Thread.sleep(period);
			} catch (InterruptedException e) {
				thread = null;
				break;
			}
		}
	}
	
}
