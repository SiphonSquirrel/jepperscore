package jepperscore.dao.model;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.DateTime;

/**
 * This represents a single round played.
 * @author Chuck
 *
 */
@XmlRootElement(name="round")
@XmlAccessorType(XmlAccessType.NONE)
public class Round {

	/**
	 * The start of the round.
	 */
	@XmlAttribute(required=true)
	private DateTime start;

	/**
	 * The end of the round.
	 */
	@XmlAttribute(required=false)
	private DateTime end;

	/**
	 * The game the round is associated with.
	 */
	@XmlElement(required=false)
	private Game game;

	/**
	 * The map being played.
	 */
	@XmlAttribute(required=true)
	private String map;

	/**
	 * @return The time the match started.
	 */
	@Nonnull
	public DateTime getStart() {
		return start;
	}

	/**
	 * Sets the start of the round.
	 * @param start The round start.
	 */
	public void setStart(@Nonnull DateTime start) {
		this.start = start;
	}

	/**
	 * @return The time the round ended, or null for in progress.
	 */
	@CheckForNull
	public DateTime getEnd() {
		return end;
	}

	/**
	 * Sets the time the round ended.
	 * @param end The time the round ended.
	 */
	public void setEnd(@Nullable DateTime end) {
		this.end = end;
	}

	/**
	 * @return The game being played when the round ended.
	 */
	@Nonnull
	public Game getGame() {
		return game;
	}

	/**
	 * Sets the game being played for this round.
	 * @param game The game being played.
	 */
	public void setGame(@Nonnull Game game) {
		this.game = game;
	}

}
