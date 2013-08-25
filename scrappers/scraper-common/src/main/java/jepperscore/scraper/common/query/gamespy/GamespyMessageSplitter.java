package jepperscore.scraper.common.query.gamespy;

/**
 * This interface is used to split Gamespy messages into callback info instances.
 * @author Chuck
 *
 */
public interface GamespyMessageSplitter {
	/**
	 * This function takes the split message and converts it into a {@link GamespyQueryCallbackInfo}.
	 * @param queryType The query type.
	 * @param messageArray The split message to convert.
	 * @return The converted message.
	 */
	GamespyQueryCallbackInfo splitMessage(String queryType, String[] messageArray);
}
