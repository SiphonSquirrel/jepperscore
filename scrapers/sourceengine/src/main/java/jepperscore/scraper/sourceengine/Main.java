package jepperscore.scraper.sourceengine;

import java.util.Arrays;

import org.apache.commons.cli.ParseException;

/**
 * This main class delegates to the Scraper class depending on the first argument.
 * @author Chuck
 *
 */
public class Main {

	/**
	 * This main function delegates to the Scraper main.
	 * @param args The command line arguments.
	 * @throws ParseException Thrown from sub-main.
	 */
	public static void main(String[] args) throws ParseException {
		if (args.length == 0) {
			throw new RuntimeException("Please specify Scraper as the first argument.");
		}

		String[] remainingArgs = args.length == 1 ? new String[0] : Arrays.copyOfRange(args, 1, args.length);
		if ("Scraper".equalsIgnoreCase(args[0])) {
			ScraperMain.main(remainingArgs);
		} else {
			throw new RuntimeException("Please specify Scraper as the first argument.");
		}
	}

}
