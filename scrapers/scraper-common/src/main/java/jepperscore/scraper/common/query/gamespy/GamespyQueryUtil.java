package jepperscore.scraper.common.query.gamespy;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * This class provides utility functions for gamespy query based games.
 *
 * @author Chuck
 *
 */
public final class GamespyQueryUtil {
	/**
	 * Hide default constructor.
	 */
	private GamespyQueryUtil() {
	}

	/**
	 * Parses the player fields from a Gamespy Query message array.
	 *
	 * @param messageArray
	 *            The message array to parse.
	 * @param startIndex
	 *            The index to start from.
	 * @return The parsed player properties in the form of Map<ID,
	 *         Map<ProperyName, PropertyValue>>.
	 */
	public static Map<String, Map<String, String>> parsePlayers(
			@Nonnull String[] messageArray, int startIndex) {
		return parsePlayers(messageArray, startIndex, new String[] {});
	}

	/**
	 * Parses the player fields from a Gamespy Query message array.
	 *
	 * @param messageArray
	 *            The message array to parse.
	 * @param startIndex
	 *            The index to start from.
	 * @param ignoreKeys
	 *            Any keys to ignore.
	 * @return The parsed player properties in the form of Map<ID,
	 *         Map<ProperyName, PropertyValue>>.
	 */
	public static Map<String, Map<String, String>> parsePlayers(
			@Nonnull String[] messageArray, int startIndex,
			@Nonnull String[] ignoreKeys) {
		Map<String, Map<String, String>> playerInfo = new HashMap<String, Map<String, String>>();

		for (int i = startIndex; i < (messageArray.length - 1); i += 2) {
			String key = messageArray[i];
			String value = messageArray[i + 1];
			String index = "";

			Map<String, String> playerProperties = null;

			int pos = key.lastIndexOf("_");
			if (pos > 0) {
				index = key.substring(pos + 1);
				key = key.substring(0, pos);

				boolean ignore = false;
				for (String ignoreKey : ignoreKeys) {
					if (key.startsWith(ignoreKey)) {
						ignore = true;
						break;
					}
				}

				if (!ignore) {
					playerProperties = playerInfo.get(index);
					if (playerProperties == null) {
						playerProperties = new HashMap<String, String>();
						playerInfo.put(index, playerProperties);
					}
				} else {
					continue;
				}
			} else {
				continue;
			}

			playerProperties.put(key, value);
		}

		return playerInfo;
	}
}
