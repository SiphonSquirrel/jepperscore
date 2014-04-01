package jepperscore.tools.jeppervcr;

import java.util.Arrays;

import org.apache.commons.cli.ParseException;

/**
 * This main class delegates to the Play or Record class depending on the first argument.
 * @author Chuck
 *
 */
public class Main {

	/**
	 * This main function delegates to either the Play or Record mains.
	 * @param args The command line arguments.
	 * @throws ParseException Thrown from Play or Record.
	 */
	public static void main(String[] args) throws ParseException {
		if (args.length == 0) {
			throw new RuntimeException("Please specify either Play or Record as the first argument.");
		}

		String[] remainingArgs = args.length == 1 ? new String[0] : Arrays.copyOfRange(args, 1, args.length - 1);
		if ("Play".equalsIgnoreCase(args[0])) {
			Play.main(remainingArgs);
		} else if ("Record".equalsIgnoreCase(args[0])) {
			Record.main(remainingArgs);
		} else {
			throw new RuntimeException("Please specify either Play or Record as the first argument.");
		}
	}

}
