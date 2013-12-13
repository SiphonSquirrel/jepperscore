package jepperscore.scraper.common;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jepperscore.dao.model.Game;

/**
 * This interface tracks the current game across all scraping methods (Log file,
 * query, RCON, etc.).
 *
 * @author Chuck
 *
 */
public interface GameManager extends BaseDataManager {
	/**
	 * This function takes a game and merges it with the existing game
	 * definition.
	 * @param game The game record to merge.
	 * @return The updated game record.
	 */
	Game provideGameRecord(@Nonnull Game game);

	/**
	 * Provides the latest game record, null if none has been provided.
	 * @return The game record.
	 */
	@CheckForNull
	Game getCurrentGame();
}
