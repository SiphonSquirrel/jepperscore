package jepperscore.scraper.common.query;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import jepperscore.dao.model.Alias;
import jepperscore.dao.model.Round;
import jepperscore.dao.model.Score;
import jepperscore.dao.model.ServerMetadata;

/**
 * This class contains the callback infomation for query clients.
 * 
 * @author Chuck
 * 
 */
public class QueryCallbackInfo {

	/**
	 * The server metadata.
	 */
	private ServerMetadata serverMetadata;

	/**
	 * The round.
	 */
	private Round round;

	/**
	 * Any players.
	 */
	private Collection<Alias> players = new LinkedList<Alias>();

	/**
	 * Any scores.
	 */
	private List<Score> scores = new LinkedList<Score>();

	/**
	 * @return The server metadata.
	 */
	public ServerMetadata getServerMetadata() {
		return serverMetadata;
	}

	/**
	 * Sets the server metadata.
	 * @param serverMetadata The server metadata.
	 */
	public void setServerMetadata(ServerMetadata serverMetadata) {
		this.serverMetadata = serverMetadata;
	}

	/**
	 * @return The round.
	 */
	public Round getRound() {
		return round;
	}

	/**
	 * Sets the round.
	 * @param round Sets the round.
	 */
	public void setRound(Round round) {
		this.round = round;
	}

	/**
	 * @return The list of players.
	 */
	public Collection<Alias> getPlayers() {
		return players;
	}

	/**
	 * Sets the list of players.
	 * @param players The list of players.
	 */
	public void setPlayers(Collection<Alias> players) {
		this.players = players;
	}

	/**
	 * @return The list of scores.
	 */
	public List<Score> getScores() {
		return scores;
	}

	/**
	 * Sets the scores.
	 * @param scores The scores.
	 */
	public void setScores(List<Score> scores) {
		this.scores = scores;
	}

}
