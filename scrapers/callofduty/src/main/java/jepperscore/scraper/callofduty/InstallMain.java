package jepperscore.scraper.callofduty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;

/**
 * This main class installs the scraper's dependencies.
 *
 * @author Chuck
 *
 */
public class InstallMain {

	/**
	 * Specifies Call of Duty's install directory.
	 */
	private static final String COD_DIRECTORY_ARG = "d";

	/**
	 * Specifies the version of Call of Duty.
	 */
	private static final String COD_VERSION_ARG = "v";

	/**
	 * Specifies the mod to install for.
	 */
	private static final String MOD_ARG = "m";

	/**
	 * Specifies the .cfg file to output to.
	 */
	private static final String CONFIG_FILENAME_ARG = "o";

	/**
	 * The path of the config in the JAR.
	 */
	private static final String CONFIG_RES_PATH = "/stats.cfg";

	/**
	 * The main function.
	 *
	 * @param args
	 *            See option setup.
	 * @throws ParseException
	 *             Exception throw from parsing problems.
	 */
	public static void main(String[] args) throws ParseException {
		Options options = new Options();

		options.addOption(COD_DIRECTORY_ARG, true,
				"Specifies Call of Duty's install directory.");
		options.addOption(COD_VERSION_ARG, true,
				"Specifies the version of Call of Duty. Values: "
						+ CoDConstants.SUPPORTED_VERSIONS);
		options.addOption(MOD_ARG, true, "Specifies the mod to install for.");
		options.addOption(CONFIG_FILENAME_ARG, true,
				"Specifies the .cfg file to output to.");

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(options, args);

		if (!cmd.hasOption(COD_DIRECTORY_ARG)) {
			throw new RuntimeException(
					"Incorrect arguments! Need -d [UT Install Directory] {-i [Server Ini Filename]} {-o [Output Ini Filename]}");
		}

		File baseDirectory = new File(cmd.getOptionValue(COD_DIRECTORY_ARG));
		if (!baseDirectory.exists()) {
			throw new RuntimeException("Directory does not exist: "
					+ baseDirectory.getAbsolutePath());
		}

		CodVersion version = CodVersion.Unknown;
		File windowsExecutable = null;
		File linuxExecutable = null;

		if (!cmd.hasOption(COD_VERSION_ARG)) {
			for (String file : baseDirectory.list()) {
				switch (file) {
				case "iw3mp.exe":
					version = CodVersion.COD4;
					windowsExecutable = new File(baseDirectory, file);
					break;

				case "cod4_lnxded":
					version = CodVersion.COD4;
					linuxExecutable = new File(baseDirectory, file);
					break;
				default:
					break;
				}
				if (version != CodVersion.Unknown) {
					break;
				}
			}
		} else {
			version = CodVersion.valueOf(cmd.getOptionValue(COD_VERSION_ARG));
		}

		if (version == CodVersion.Unknown) {
			throw new RuntimeException(
					"Unable to detect Call of Duty version, supported versions are: "
							+ CoDConstants.SUPPORTED_VERSIONS);
		}

		File configDirectory;
		if (!cmd.hasOption(MOD_ARG)) {
			configDirectory = new File(baseDirectory, "main");
		} else {
			configDirectory = new File(baseDirectory, "Mods" + File.separator
					+ cmd.getOptionValue(MOD_ARG));
		}

		if (!configDirectory.exists()) {
			throw new RuntimeException(
					"Could not find configuration directory: "
							+ configDirectory.getAbsolutePath());
		}

		File configFile = new File(configDirectory, cmd.getOptionValue(
				CONFIG_FILENAME_ARG, "stats.cfg"));

		System.out.println("Installing Server Config ("
				+ configFile.getAbsolutePath() + ")...");
		InputStream configIn = null;
		FileOutputStream configOut = null;
		try {
			configIn = InstallMain.class.getResourceAsStream(CONFIG_RES_PATH);
			configOut = new FileOutputStream(configFile);

			IOUtils.copy(configIn, configOut);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(configOut);
			IOUtils.closeQuietly(configIn);
		}

		String defaultArgs;
		switch (version) {
		case COD4:
			defaultArgs = " +set g_gametype dm +map mp_showdown";
			break;
		default:
			defaultArgs = "";
			break;
		}

		if (windowsExecutable != null) {
			File batchScript = new File(baseDirectory, "start"
					+ cmd.getOptionValue(MOD_ARG, "") + "ServerWithStats.bat");
			if (batchScript.exists()) {
				System.out.println("Skipping creation of batch script ("
						+ batchScript.getName() + "), file exists...");
			} else {
				PrintStream scriptWriter = null;

				try {
					scriptWriter = new PrintStream(batchScript, "UTF-8");

					scriptWriter.println("@echo off");
					scriptWriter.println(windowsExecutable.getName()
							+ " +exec stats.cfg +set dedicated 1"
							+ defaultArgs
							+ (cmd.hasOption(MOD_ARG) ? " +set fs_game Mods/"
									+ cmd.getOptionValue(MOD_ARG) : ""));

				} catch (IOException e) {
					throw new RuntimeException(e);
				} finally {
					IOUtils.closeQuietly(scriptWriter);
				}

				System.out.println("Wrote Windows server start up script: "
						+ batchScript.getName());
			}
		}

		if (linuxExecutable != null) {
			File shScript = new File(baseDirectory, "start"
					+ cmd.getOptionValue(MOD_ARG, "") + "ServerWithStats.sh");
			if (shScript.exists()) {
				System.out.println("Skipping creation of bash script ("
						+ shScript.getName() + "), file exists...");
			} else {
				PrintStream scriptWriter = null;

				try {
					scriptWriter = new PrintStream(shScript, "UTF-8");

					scriptWriter.println("#!/bin/bash");
					scriptWriter.println(linuxExecutable.getName()
							+ " +exec stats.cfg +set dedicated 1"
							+ defaultArgs
							+ (cmd.hasOption(MOD_ARG) ? " +set fs_game Mods/"
									+ cmd.getOptionValue(MOD_ARG) : ""));

				} catch (IOException e) {
					throw new RuntimeException(e);
				} finally {
					IOUtils.closeQuietly(scriptWriter);
				}
				System.out.println("Wrote Linux server start up script: "
						+ shScript.getName());
			}
		}
		System.out.println("Done!");
	}
}
