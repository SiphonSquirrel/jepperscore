package jepperscore.scraper.common;

/**
 * This is the base datamanager for tracking information across all scraping
 * methods (Log file, query, RCON, etc.).
 *
 * @author Chuck
 *
 */
public interface BaseDataManager {
	/**
	 * This function resets all knowledge in preparation for a new round.
	 */
	void newRound();
}
