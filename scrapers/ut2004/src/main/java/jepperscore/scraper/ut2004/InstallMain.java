package jepperscore.scraper.ut2004;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * This main class installs the scraper's dependencies.
 *
 * @author Chuck
 *
 */
public class InstallMain {

	/**
	 * The OLStats Zip location in the JAR.
	 */
	private static final String OLSTATS_ZIP_PATH = "/olstats-3.01.zip";

	/**
	 * The top directory in the OLStats zip.
	 */
	private static final String OLSTATS_TOP_DIRECTORY = "olstats-3.01/";

	/**
	 * Specifies the UT's install directory.
	 */
	public static String UT_DIRECTORY_ARG = "d";

	/**
	 * Specifies the .ini file to use as a base.
	 */
	public static String BASE_INI_FILENAME_ARG = "i";

	/**
	 * Specifies the .ini file to output to.
	 */
	public static String OUT_INI_FILENAME_ARG = "o";

	/**
	 * The default in ini file.
	 */
	public static String DEFAULT_IN_INI_FILENAME = "Default.ini";

	/**
	 * The default out ini file.
	 */
	public static String DEFAULT_OUT_INI_FILENAME = "Stats.ini";

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

		options.addOption(UT_DIRECTORY_ARG, true,
				"Specifies the UT's install directory.");
		options.addOption(BASE_INI_FILENAME_ARG, true,
				"Specifies the .ini file to use as a base.");
		options.addOption(OUT_INI_FILENAME_ARG, true,
				"Specifies the .ini file to output to.");

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(options, args);

		if (!cmd.hasOption(UT_DIRECTORY_ARG)) {
			throw new RuntimeException(
					"Incorrect arguments! Need -d [UT Install Directory] {-i [Server Ini Filename]} {-o [Output Ini Filename]}");
		}

		File baseDirectory = new File(cmd.getOptionValue(UT_DIRECTORY_ARG));
		File inFile = new File(cmd.getOptionValue(BASE_INI_FILENAME_ARG,
				DEFAULT_IN_INI_FILENAME));
		File outFile = new File(cmd.getOptionValue(OUT_INI_FILENAME_ARG,
				DEFAULT_OUT_INI_FILENAME));

		if (!baseDirectory.exists()) {
			throw new RuntimeException("Directory '"
					+ baseDirectory.getAbsolutePath() + "' does not exist.");
		}

		File systemDirectory = new File(baseDirectory, "System");

		if (!systemDirectory.exists()) {
			throw new RuntimeException("Directory '"
					+ systemDirectory.getAbsolutePath() + "' does not exist.");
		}

		if (!inFile.exists()) {
			File systemInFile = new File(systemDirectory, cmd.getOptionValue(
					BASE_INI_FILENAME_ARG, DEFAULT_IN_INI_FILENAME));
			if (!systemInFile.exists()) {
				throw new RuntimeException("Cannot find " + inFile.getName()
						+ " at " + inFile.getAbsolutePath() + " or "
						+ systemInFile.getAbsolutePath());
			}
			inFile = systemInFile;
		}

		// Adjust file location to be in the System directory if only a filename
		// was passed.
		if (outFile.getPath().equals(
				cmd.getOptionValue(OUT_INI_FILENAME_ARG,
						DEFAULT_OUT_INI_FILENAME))) {
			outFile = new File(systemDirectory, outFile.getName());
		}

		if (outFile.exists()) {
			throw new RuntimeException(outFile.getAbsolutePath()
					+ " exists! Please remove before continuing.");
		}

		System.out.println("Installing OLStats Mutator...");
		InputStream olstatsStream = null;
		ZipInputStream zipStream = null;
		try {
			olstatsStream = InstallMain.class
					.getResourceAsStream(OLSTATS_ZIP_PATH);
			zipStream = new ZipInputStream(olstatsStream);

			ZipEntry entry = null;
			while ((entry = zipStream.getNextEntry()) != null) {
				File entryName = new File(entry.getName().substring(
						OLSTATS_TOP_DIRECTORY.length()));
				if ((entryName.getParentFile() != null)
						&& "System".equals(entryName.getParentFile().getName())) {

					if (entryName.getName().startsWith("OLStats.")
							|| entryName.getName()
									.startsWith("OLStats_Readme.")
							|| entryName.getName().startsWith("LibHTTP2")) {
						File dest = new File(baseDirectory, entryName.getPath());

						System.out.println(" Installing " + entryName.getPath()
								+ " to " + dest.getAbsolutePath());
						OutputStream os = null;
						try {
							os = new FileOutputStream(dest);
							IOUtils.copy(zipStream, os);
						} finally {
							IOUtils.closeQuietly(os);
						}
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(zipStream);
			IOUtils.closeQuietly(olstatsStream);
		}

		System.out.println("Installing Server Config ("
				+ outFile.getAbsolutePath() + ")...");

		// Patch UT2004.ini
		IniPatcher statsIniPatcher = new IniPatcher(inFile, outFile);
		statsIniPatcher.addPatch("Engine.GameInfo",
				"GameStatsClass", "OLStats.OLGameStats");
		statsIniPatcher.execute();

		// Patch OLStats.ini
		File olStatsIni = new File(systemDirectory, "OLStats.ini");
		File olStatsTempIni = new File(systemDirectory, "OLStats.ini.tmp");

		IniPatcher olStatsIniPatcher = new IniPatcher(olStatsIni,
				olStatsTempIni);
		olStatsIniPatcher
				.addPatch("OLStats.OLLocalGameStats", "bDebug", "True");
		olStatsIniPatcher.execute();

		try {
			FileUtils.deleteQuietly(olStatsIni);
			FileUtils.moveFile(olStatsTempIni, olStatsIni);
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}

		File batchScript = new File(systemDirectory, "startServerWithStats.bat");
		if (batchScript.exists()) {
			System.out.println("Skipping creation of batch script ("
					+ batchScript.getName() + "), file exists...");
		} else {
			PrintStream scriptWriter = null;

			try {
				scriptWriter = new PrintStream(batchScript);

				scriptWriter.println("@echo off");
				scriptWriter.println("echo Writing server log to server.log");
				scriptWriter.println(":10");
				scriptWriter
						.println("ucc server DM-Rankin?game=XGame.xDeathMatch?GameStats=True ini="
								+ outFile.getName() + " > server.log");
				scriptWriter.println("copy server.log servercrash.log");
				scriptWriter.println("goto 10");

			} catch (IOException e) {
				new RuntimeException(e);
			} finally {
				IOUtils.closeQuietly(scriptWriter);
			}

			System.out.println("Wrote Windows server start up script: "
					+ batchScript.getName());
		}

		File shScript = new File(systemDirectory, "startServerWithStats.sh");
		if (shScript.exists()) {
			System.out.println("Skipping creation of bash script ("
					+ shScript.getName() + "), file exists...");
		} else {
			PrintStream scriptWriter = null;

			try {
				scriptWriter = new PrintStream(shScript);

				scriptWriter.println("#!/bin/bash");
				scriptWriter.println("echo 'Writing server log to server.log'");
				scriptWriter.println("while true");
				scriptWriter.println("do");
				scriptWriter
						.println("./ucc-bin server DM-Rankin?game=XGame.xDeathMatch?GameStats=True ini="
								+ outFile.getName()
								+ " -nohomedir &> server.log");
				scriptWriter.println("DATE=20`date +%y%m%d`-`date +%H%M%S`");
				scriptWriter.println("mv server.log servercrash-$DATE.log");

				scriptWriter.println("done");

			} catch (IOException e) {
				new RuntimeException(e);
			} finally {
				IOUtils.closeQuietly(scriptWriter);
			}
			System.out.println("Wrote Linux server start up script: "
					+ shScript.getName());
		}

		System.out.println("Done!");
	}
}
