package jepperscore.scraper.bf1942;

import jepperscore.scraper.common.Scraper;
import jepperscore.scraper.common.ScraperStatus;

/**
 * This scraper works for BF1942.
 * @author Chuck
 *
 */
public class BF1942Scraper implements Scraper {

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
