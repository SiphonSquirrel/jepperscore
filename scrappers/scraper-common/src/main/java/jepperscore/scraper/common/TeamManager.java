package jepperscore.scraper.common;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jepperscore.dao.model.Team;

/**
 * This interface tracks the current game across all scraping methods (Log file,
 * query, RCON, etc.).
 *
 * @author Chuck
 *
 */
public interface TeamManager extends BaseDataManager {
	/**
	 * This function takes a team and merges it with an existing team
	 * definition.
	 * @param team The team record to merge.
	 * @return The updated team record.
	 */
	Team provideTeamRecord(@Nonnull Team team);

	/**
	 * Provides the team record by name, null if none has been provided.
	 * @param name The name of the team.
	 * @return The team record.
	 */
	@CheckForNull
	Team getTeamByName(@Nonnull String name);
}
