package jepperscore.scraper.common;

/**
 * This enumeration is so that scrapers can report a standardized status.
 * @author Chuck
 *
 */
public enum ScraperStatus {
	/**
	 * The scraper has not yet been started.
	 */
	NotRunning,

	/**
	 * There is a problem with the scraper or its configuration.
	 */
	InError,

	/**
	 * The scraper is initializing.
	 */
	Initializing,

	/**
	 * The scraper is unable to get events, only score updates.
	 */
	ScoresOnly,

	/**
	 * The scraper is unable to get score updates, just event.
	 */
	EventsOnly,

	/**
	 * The scraper is functioning properly, and receiving all data.
	 */
	AllData
}
