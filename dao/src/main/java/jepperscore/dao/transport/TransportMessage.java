package jepperscore.dao.transport;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import jepperscore.dao.model.Alias;
import jepperscore.dao.model.Event;
import jepperscore.dao.model.Round;
import jepperscore.dao.model.Score;
import jepperscore.dao.model.ServerMetadata;
import jepperscore.dao.model.Team;

/**
 * This class provides the XML message for sending events or alias across the
 * wire.
 *
 * @author Chuck
 *
 */
@XmlRootElement(name = "message")
@XmlAccessorType(XmlAccessType.NONE)
public class TransportMessage {
	/**
	 * ServerMetadata of the message.
	 */
	private ServerMetadata serverMetadata;

	/**
	 * Round of the message.
	 */
	private Round round;

	/**
	 * Event of the message.
	 */
	private Event event;

	/**
	 * Alias of the message.
	 */
	private Alias alias;

	/**
	 * Score of the message.
	 */
	private Score score;

	/**
	 * Team of the message.
	 */
	private Team team;

	/**
	 * @return The message content.
	 */
	@XmlElements(value = {
			@XmlElement(name = "serverMetadata", type = ServerMetadata.class),
			@XmlElement(name = "round", type = Round.class),
			@XmlElement(name = "event", type = Event.class),
			@XmlElement(name = "alias", type = Alias.class),
			@XmlElement(name = "score", type = Score.class),
			@XmlElement(name = "team", type = Team.class) })
	@CheckForNull
	public Object getMessageContent() {
		if (getServerMetadata() != null) {
			return getServerMetadata();
		} else if (round != null) {
			return round;
		} else if (event != null) {
			return event;
		} else if (alias != null) {
			return alias;
		} else if (score != null) {
			return score;
		} else if (getTeam() != null) {
			return getTeam();
		} else {
			return null;
		}
	}

	/**
	 * Sets the message content.
	 *
	 * @param content
	 *            The content of the message.
	 */
	public void setMessageContent(@Nonnull Object content) {
		if (content instanceof ServerMetadata) {
			setServerMetadata((ServerMetadata) content);
		} else if (content instanceof Round) {
			round = (Round) content;
		} else if (content instanceof Event) {
			event = (Event) content;
		} else if (content instanceof Alias) {
			alias = (Alias) content;
		} else if (content instanceof Score) {
			score = (Score) content;
		} else if (content instanceof Team) {
			setTeam((Team) content);
		}
	}

	/**
	 * @return The server metadata.
	 */
	public ServerMetadata getServerMetadata() {
		return serverMetadata;
	}

	/**
	 * @param serverMetadata
	 *            The server metadata to set.
	 */
	public void setServerMetadata(ServerMetadata serverMetadata) {
		this.serverMetadata = serverMetadata;
	}

	/**
	 * @return The round. Represents the start or end of a round.
	 */
	@CheckForNull
	public Round getRound() {
		return round;
	}

	/**
	 * @param round
	 *            The round to set. Represents the start or end of a round.
	 */
	public void setRound(@Nullable Round round) {
		this.round = round;
	}

	/**
	 * @return The event.
	 */
	@CheckForNull
	public Event getEvent() {
		return event;
	}

	/**
	 * @param event
	 *            The event.
	 */
	public void setEvent(@Nullable Event event) {
		this.event = event;
	}

	/**
	 * @return The event.
	 */
	@CheckForNull
	public Alias getAlias() {
		return alias;
	}

	/**
	 * @param alias
	 *            The alias.
	 */
	public void setAlias(@Nullable Alias alias) {
		this.alias = alias;
	}

	/**
	 * @return The score.
	 */
	@CheckForNull
	public Score getScore() {
		return score;
	}

	/**
	 * @param score
	 *            The score to set.
	 */
	public void setScore(@Nullable Score score) {
		this.score = score;
	}

	/**
	 * @return The team.
	 */
	public Team getTeam() {
		return team;
	}

	/**
	 * @param team
	 *            The team to set.
	 */
	public void setTeam(Team team) {
		this.team = team;
	}
}