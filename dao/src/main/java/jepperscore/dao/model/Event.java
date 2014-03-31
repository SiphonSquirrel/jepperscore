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
 * This class represents an event (player score, team score, player leave/join,
 * team membership change, etc.
 *
 * @author Chuck
 *
 */
@XmlRootElement(name = "event")
@XmlAccessorType(XmlAccessType.NONE)
public class Event {

	/**
	 * The timestamp of the event.
	 */
	@XmlAttribute(required = true)
	@JsonProperty
	@JsonSerialize(converter=JodaTimeToString.class)
	@JsonDeserialize(converter=StringToJodaTime.class)
	private DateTime timestamp;

	/**
	 * The victim of the event.
	 */
	@XmlElement(required = false)
	@JsonProperty
	private Alias victim;

	/**
	 * The attacker of the event.
	 */
	@XmlElement(required = false)
	@JsonProperty
	private Alias attacker;

	/**
	 * The text of the event.
	 */
	@XmlElement(required = false)
	@JsonProperty
	private String eventText;

	/**
	 * The event code.
	 */
	@XmlElement(required = false)
	@JsonProperty
	private EventCode eventCode;

	/**
	 * The round the event is associated with.
	 */
	@XmlElement(required = false)
	@JsonProperty
	private Round round;

	/**
	 * @return The timestamp when the event happened.
	 */
	@Nonnull
	public DateTime getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets the event timestamp.
	 *
	 * @param timestamp
	 *            When the event happened.
	 */
	public void setTimestamp(@Nonnull DateTime timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return The player who the event happened to.
	 */
	@CheckForNull
	public Alias getVictim() {
		return victim;
	}

	/**
	 * Sets the player who the event happened to.
	 *
	 * @param victim
	 *            The player who the event happened to.
	 */
	public void setVictim(@Nullable Alias victim) {
		this.victim = victim;
	}

	/**
	 * @return The player who caused the event.
	 */
	@CheckForNull
	public Alias getAttacker() {
		return attacker;
	}

	/**
	 * Sets the player who caused the event.
	 *
	 * @param attacker
	 *            The player who started the event.
	 */
	public void setAttacker(@Nullable Alias attacker) {
		this.attacker = attacker;
	}

	/**
	 * @return The event text associated with the event.
	 */
	@Nonnull
	public String getEventText() {
		if (eventText == null) {
			return "";
		}
		return eventText;
	}

	/**
	 * Sets the event text associated with the event.
	 *
	 * @param eventText
	 *            The event text.
	 */
	public void setEventText(@Nullable String eventText) {
		this.eventText = eventText;
	}

	/**
	 * @return The event code associated with the event.
	 */
	@Nullable
	public EventCode getEventCode() {
		return eventCode;
	}

	/**
	 * Sets the event code for the event.
	 *
	 * @param eventCode
	 *            The event code to set.
	 */
	public void setEventCode(@Nullable EventCode eventCode) {
		this.eventCode = eventCode;
	}

	/**
	 * The round associated with the event.
	 *
	 * @return The round.
	 */
	@Nonnull
	public Round getRound() {
		return round;
	}

	/**
	 * Sets the round associated with the event.
	 *
	 * @param round
	 *            The round.
	 */
	public void setRound(@Nonnull Round round) {
		this.round = round;
	}

	/**
	 * @return The event text with the placeholders parsed.
	 */
	@Nonnull
	public String getParsedEventText() {
		String text = getEventText();

		Alias attacker = getAttacker();
		Alias victim = getVictim();

		if (attacker != null) {
			text = text.replaceAll("\\{attacker\\}", attacker.getName());
		}

		if (victim != null) {
			text = text.replaceAll("\\{victim\\}", victim.getName());
		}

		return text;
	}
}
