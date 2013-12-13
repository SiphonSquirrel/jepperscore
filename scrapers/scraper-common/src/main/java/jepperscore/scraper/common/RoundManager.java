package jepperscore.scraper.common;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jepperscore.dao.model.Round;

/**
 * This interface tracks the current round across all scraping methods (Log file,
 * query, RCON, etc.).
 *
 * @author Chuck
 *
 */
public interface RoundManager extends BaseDataManager {
	/**
	 * This function takes an round and merges it with the existing round
	 * definition.
	 * @param round The round to merge.
	 * @return The updated round.
	 */
	Round provideRoundRecord(@Nonnull Round round);

	/**
	 * Provides the latest round record, null if none has been provided.
	 * @return The round record.
	 */
	@CheckForNull
	Round getCurrentRound();
}
