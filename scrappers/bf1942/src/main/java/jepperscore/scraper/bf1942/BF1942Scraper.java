package jepperscore.scraper.bf1942;

import javax.jms.Topic;

import jepperscore.scraper.common.Scraper;
import jepperscore.scraper.common.ScraperStatus;

/**
 * This scraper works for BF1942.
 * @author Chuck
 *
 */
public class BF1942Scraper implements Scraper, Runnable {

	/**
	 * The status of the scraper.
	 */
	private ScraperStatus status = ScraperStatus.NotRunning;
	
	/**
	 * The current ActiveMQ event topic.
	 */
	private Topic eventTopic;
	
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

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
