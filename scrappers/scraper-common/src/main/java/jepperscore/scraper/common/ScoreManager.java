package jepperscore.scraper.common;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jepperscore.dao.model.Alias;
import jepperscore.dao.model.Score;

/**
 * This interface tracks the current game across all scraping methods (Log file,
 * query, RCON, etc.).
 *
 * @author Chuck
 *
 */
public interface ScoreManager extends BaseDataManager {
	/**
	 * This function takes a score and merges it with the existing game
	 * definition.
	 * @param score The score record to merge.
	 * @return The updated score.
	 */
	Score provideScoreRecord(@Nonnull Score score);

	/**
	 * Returns the score for a given player.
	 * @param player The player.
	 * @return The score.
	 */
	@CheckForNull
	Score getScoreForPlayer(@Nonnull Alias player);

	/**
	 * Increments the score for a player by the specified amount.
	 * @param player The player.
	 * @param amount The amount.
	 * @return The updated score.
	 */
	Score incrementScore(@Nonnull Alias player, float amount);
}
