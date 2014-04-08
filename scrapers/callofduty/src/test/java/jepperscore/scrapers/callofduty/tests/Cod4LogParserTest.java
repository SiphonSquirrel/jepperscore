package jepperscore.scrapers.callofduty.tests;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import jepperscore.backends.testing.TestingMessageDestination;
import jepperscore.scraper.callofduty.scraper.Cod4LogParser;
import jepperscore.scraper.common.SimpleDataManager;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This test checks out the Cod4LogParser.
 * @author Chuck
 *
 */
public class Cod4LogParserTest {

	/**
	 * This test starts the log parser up against a very long log file.
	 * @throws FileNotFoundException If the test data could not be found.
	 */
	@Test
	@Ignore
	public void test() throws FileNotFoundException {
		FileInputStream is = null;
		try {
			is = new FileInputStream("testdata/cod4/games_mp.log");
			TestingMessageDestination messageDestination = new TestingMessageDestination(null);
			SimpleDataManager dataManager = new SimpleDataManager(messageDestination);

			Cod4LogParser parser = new Cod4LogParser(is, messageDestination, dataManager, dataManager);

			parser.run();
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
}
