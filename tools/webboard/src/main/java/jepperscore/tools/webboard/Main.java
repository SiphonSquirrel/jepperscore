package jepperscore.tools.webboard;

import java.util.Arrays;

import org.apache.commons.cli.ParseException;

/**
 * This main class delegates to the Play or Record class depending on the first argument.
 * @author Chuck
 *
 */
public class Main {

	/**
	 * This main function delegates to either the Installer main.
	 * @param args The command line arguments.
	 * @throws ParseException Thrown from sub-main.
	 */
	public static void main(String[] args) throws ParseException {
		if (args.length == 0) {
			throw new RuntimeException("Please specify either Install as the first argument.");
		}

		String[] remainingArgs = args.length == 1 ? new String[0] : Arrays.copyOfRange(args, 1, args.length);
		if ("Install".equalsIgnoreCase(args[0])) {
			InstallerMain.main(remainingArgs);
		} else {
			throw new RuntimeException("Please specify either Install as the first argument.");
		}
	}
}
