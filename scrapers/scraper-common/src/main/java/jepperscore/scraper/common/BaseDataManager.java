package jepperscore.scraper.common;

import jepperscore.dao.model.Round;

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

	/**
	 * This function resets all knowledge in preparation for a new round.
	 * @param r The new round object to use.
	 */
	void newRound(Round r);
}
