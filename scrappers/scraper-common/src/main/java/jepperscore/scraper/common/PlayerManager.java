package jepperscore.scraper.common;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jepperscore.dao.model.Alias;

/**
 * This interface tracks the players list across all scraping methods (Log file,
 * query, RCON, etc.).
 *
 * @author Chuck
 *
 */
public interface PlayerManager extends BaseDataManager {

	/**
	 * This function takes an alias and merges it with the existing alias
	 * definition.
	 *
	 * @param player
	 *            The player to merge.
	 * @return The merged player record.
	 */
	@Nonnull
	Alias providePlayerRecord(@Nonnull Alias player);

	/**
	 * Looks up or creates a player based on their ID.
	 *
	 * @param id
	 *            The player id.
	 * @return The player.
	 */
	Alias getPlayer(@Nonnull String id);

	/**
	 * Looks up or creates a player based on their ID.
	 *
	 * @param name
	 *            The player name.
	 * @return The player.
	 */
	@CheckForNull
	Alias getPlayerByName(@Nonnull String name);
}
