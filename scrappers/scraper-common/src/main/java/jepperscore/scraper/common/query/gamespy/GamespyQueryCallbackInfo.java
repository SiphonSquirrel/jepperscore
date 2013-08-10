package jepperscore.scraper.common.query.gamespy;

import jepperscore.scraper.common.query.QueryCallbackInfo;

/**
 * This class hold all the extra information specific to GameSpy queries.
 * @author Chuck
 *
 */
public class GamespyQueryCallbackInfo extends QueryCallbackInfo {

	/**
	 * This is the complete raw response from the server.
	 */
	private String rawResponse;

	/**
	 * @return The raw response.
	 */
	public String getRawResponse() {
		return rawResponse;
	}

	/**
	 * Sets the raw response.
	 * @param rawResponse The raw response.
	 */
	public void setRawResponse(String rawResponse) {
		this.rawResponse = rawResponse;
	}
	
}
