package jepperscore.scraper.ut2004;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;

/**
 * This class patches ini files.
 *
 * @author Chuck
 *
 */
public class IniPatcher {

	/**
	 * The file to be patched.
	 */
	private File origFile;

	/**
	 * The output file.
	 */
	private File modifiedFile;

	/**
	 * The patches to apply.
	 */
	private Map<String, Map<String, String>> patches = new HashMap<String, Map<String, String>>();

	/**
	 * This constructor sets up the patching.
	 *
	 * @param origFile
	 *            The file to be patched.
	 * @param modifiedFile
	 *            The output file.
	 */
	public IniPatcher(File origFile, File modifiedFile) {
		this.origFile = origFile;
		this.modifiedFile = modifiedFile;
	}

	/**
	 * Adds a patch.
	 *
	 * @param section
	 *            The section to patch.
	 * @param key
	 *            The key to patch.
	 * @param value
	 *            The value to set.
	 */
	public void addPatch(String section, String key, String value) {
		Map<String, String> keyValues = patches.get(section);
		if (keyValues == null) {
			keyValues = new HashMap<String, String>();
			patches.put(section, keyValues);
		}

		keyValues.put(key, value);
	}

	/**
	 * Does the patching.
	 */
	public void execute() {
		Set<String> processedSections = new HashSet<String>();

		FileInputStream configInStream = null;
		BufferedReader reader = null;
		PrintStream writer = null;
		try {
			configInStream = new FileInputStream(origFile);

			reader = new BufferedReader(new InputStreamReader(configInStream));
			writer = new PrintStream(modifiedFile);

			String line = null;
			String section = "";
			Map<String, String> patchKeyValues = null;

			while ((line = reader.readLine()) != null) {

				String trimmedLine = line.trim();
				if (trimmedLine.isEmpty() || trimmedLine.startsWith(";")) {
					writer.println(line);
					continue;
				}

				if (trimmedLine.startsWith("[")) {
					if (patchKeyValues != null) {
						if (!patchKeyValues.isEmpty()) {
							for (Entry<String, String> entry : patchKeyValues
									.entrySet()) {
								writer.println(entry.getKey() + "="
										+ entry.getValue());
							}

							writer.println();
						}
					}

					section = trimmedLine
							.substring(1, trimmedLine.indexOf(']')).trim();
					writer.println(line);
					processedSections.add(section);
					if (patches.containsKey(section)) {
						patchKeyValues = new HashMap<String, String>(
								patches.get(section));
					}
					continue;
				}

				if (patchKeyValues == null) {
					writer.println(line);
					continue;
				}

				int eqPos = trimmedLine.indexOf("=");
				if (eqPos < 0) {
					writer.println(line);
					continue;
				}

				String key = trimmedLine.substring(0, eqPos).trim();
				String newValue = patchKeyValues.remove(key);

				if (newValue == null) {
					writer.println(line);
				} else {
					writer.println(key + "=" + newValue);
				}
			}

			for (Entry<String, Map<String, String>> patch : patches.entrySet()) {
				section = patch.getKey();
				if (!processedSections.contains(section)) {
					writer.println();
					writer.println("[" + section + "]");
					for (Entry<String, String> entry : patch.getValue()
							.entrySet()) {
						writer.println(entry.getKey() + "=" + entry.getValue());
					}

					writer.println();
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(writer);
			IOUtils.closeQuietly(reader);

			IOUtils.closeQuietly(configInStream);
		}
	}
}
