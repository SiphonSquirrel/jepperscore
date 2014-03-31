package jepperscore.dao.transport;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import jepperscore.dao.model.Alias;
import jepperscore.dao.model.Event;
import jepperscore.dao.model.Round;
import jepperscore.dao.model.Score;
import jepperscore.dao.model.ServerMetadata;
import jepperscore.dao.model.Team;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * This class provides the XML message for sending events or alias across the
 * wire.
 * 
 * @author Chuck
 * 
 */
@XmlRootElement(name = "message")
@XmlAccessorType(XmlAccessType.NONE)
@JsonInclude(Include.NON_EMPTY)
//@JsonIgnoreProperties(ignoreUnknown=true)
public class TransportMessage {
	/**
	 * An identifying id.
	 */
	@XmlAttribute(required = true)
	@JsonProperty("_id")
	private String id;

	/**
	 * The revision of this record.
	 */
	@JsonProperty("_rev")
	private String revision;

	/**
	 * This ID correlates the messages to a single session.
	 */
	@JsonProperty
	private String sessionId;
	
	/**
	 * ServerMetadata of the message.
	 */
	@JsonProperty
	private ServerMetadata serverMetadata;

	/**
	 * Round of the message.
	 */
	@JsonProperty
	private Round round;

	/**
	 * Event of the message.
	 */
	@JsonProperty
	private Event event;

	/**
	 * Alias of the message.
	 */
	@JsonProperty
	private Alias alias;

	/**
	 * Score of the message.
	 */
	@JsonProperty
	private Score score;

	/**
	 * Team of the message.
	 */
	@JsonProperty
	private Team team;

	/**
	 * Default constructor.
	 */
	public TransportMessage() {
	}
	
	/**
	 * Sets the content of the message.
	 * @param content The content to set.
	 */
	public TransportMessage(Object content) {
		setMessageContent(content);
	}
	
	/**
	 * Sets the content and session of the message.
	 * @param content The content.
	 * @param session The session.
	 */
	public TransportMessage(Object content, String session) {
		this(content);
		setSessionId(session);
	}
	
	/**
	 * Sets the content and session of the message.
	 * @param id The id of the message.
	 * @param content The content.
	 * @param session The session.
	 */
	public TransportMessage(String id, Object content, String session) {
		this(content);
		setId(id);
		setSessionId(session);
	}
	
	/**
	 * @return the session id.
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * Sets the session id.
	 * @param sessionId The session id to set.
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * @return the id
	 */
	@JsonGetter("_id")
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	@JsonSetter("_id")
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the revision
	 */
	public String getRevision() {
		return revision;
	}

	/**
	 * @param revision
	 *            the revision to set
	 */
	public void setRevision(String revision) {
		this.revision = revision;
	}

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
	@JsonIgnore
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
	@JsonIgnore
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

	@Override
	public String toString() {
		Object content = getMessageContent();
		if (content == null)
			return "";
		else
			return content.toString();
	}
}
