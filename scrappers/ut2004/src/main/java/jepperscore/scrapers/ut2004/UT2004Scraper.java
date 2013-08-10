package jepperscore.scrapers.ut2004;

import jepperscore.scraper.common.Scraper;
import jepperscore.scraper.common.ScraperStatus;

/**
 * This scraper works with UT2004 + mods.
 * @author Chuck
 *
 */
public class UT2004Scraper implements Scraper {

	/**
	 * The status of the scraper.
	 */
	private ScraperStatus status = ScraperStatus.NotRunning;

	@Override
	public ScraperStatus getStatus() {
		return status;
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}
}
