package jepperscore.scrapers.etl.tests;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import jepperscore.backends.testing.TestingMessageDestination;
import jepperscore.scraper.common.SimpleDataManager;
import jepperscore.scraper.etl.scraper.ETLLogParser;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This test checks out the Cod4LogParser.
 * @author Chuck
 *
 */
public class ETLLogParserTest {

	/**
	 * This test starts the log parser up against a very long log file.
	 * @throws FileNotFoundException If the test data could not be found.
	 */
	@Test
	@Ignore
	public void test() throws FileNotFoundException {
		FileInputStream is = null;
		try {
			is = new FileInputStream("testdata/etconsole.log");
			TestingMessageDestination messageDestination = new TestingMessageDestination(null);
			SimpleDataManager dataManager = new SimpleDataManager(messageDestination);

			ETLLogParser parser = new ETLLogParser(is, messageDestination, dataManager, dataManager, dataManager);

			parser.run();
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
}
