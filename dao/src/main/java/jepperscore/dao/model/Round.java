package jepperscore.dao.model;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import jepperscore.dao.model.converter.JodaTimeToString;
import jepperscore.dao.model.converter.StringToJodaTime;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * This represents a single round played.
 * @author Chuck
 *
 */
@XmlRootElement(name="round")
@XmlAccessorType(XmlAccessType.NONE)
public class Round {

	/**
	 * The ID of the round.
	 */
	@XmlAttribute(required=true)
	@JsonProperty
	private String id;

	/**
	 * The start of the round.
	 */
	@XmlAttribute(required=true)
	@JsonProperty
	@JsonSerialize(converter=JodaTimeToString.class)
	@JsonDeserialize(converter=StringToJodaTime.class)
	private DateTime start;

	/**
	 * The end of the round.
	 */
	@XmlAttribute(required=false)
	@JsonProperty
	@JsonSerialize(converter=JodaTimeToString.class)
	@JsonDeserialize(converter=StringToJodaTime.class)
	private DateTime end;

	/**
	 * The game the round is associated with.
	 */
	@XmlElement(required=false)
	@JsonProperty
	private Game game;

	/**
	 * The map being played.
	 */
	@XmlAttribute(required=true)
	@JsonProperty
	private String map;

	/**
	 * The default constructor.
	 */
	public Round() {

	}

	/**
	 * Constructor with all parameters.
	 * @param id The ID of the round.
	 * @param start The start time of the round.
	 * @param end The end time of the round.
	 * @param game The game of the round.
	 * @param map The map of the round.
	 */
	public Round(String id, DateTime start, DateTime end, Game game, String map) {
		this.id = id;
		this.start = start;
		this.end = end;
		this.game = game;
		this.map = map;
	}

	/**
	 * @return The id of the round.
	 */
	@Nonnull
	public String getId() {
		if (id == null) {
			return "";
		}
		return id;
	}

	/**
	 * Sets the ID.
	 *
	 * @param id
	 *            The ID of the round.
	 */
	public void setId(@Nonnull String id) {
		this.id = id;
	}


	/**
	 * @return The time the match started.
	 */
	@CheckForNull
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

	/**
	 * @return The map.
	 */
	public String getMap() {
		return map;
	}

	/**
	 * @param map The map.
	 */
	public void setMap(String map) {
		this.map = map;
	}

	/**
	 * @return A deep copy of this object.
	 */
	public Round copy() {
		Game newGame = game;
		if (newGame != null) {
			newGame = newGame.copy();
		}
		return new Round(id, start, end, newGame, map);
	}

}
