package jepperscore.scraper.common;

import javax.annotation.Nonnull;

/**
 * This is the common interface for scrapers.
 * @author Chuck
 *
 */
public interface Scraper {

	/**
	 * @return The status of the scraper.
	 */
	@Nonnull
	ScraperStatus getStatus();

	/**
	 * Starts the scraper.
	 */
	void start();

	/**
	 * Stops the scraper.
	 */
	void stop();

}
