package jepperscore.scraper.common.query.idtech;

/**
 * Determines what number to use as the score.
 *
 * @author Chuck
 *
 */
public enum IdTechScoreMode {
	/**
	 * Base score solely on experience.
	 */
	Experience,

	/**
	 * Base score solely on kill count.
	 */
	Kills,

	/**
	 * Base score on kills - deaths.
	 */
	KillsMinusDeaths
}
