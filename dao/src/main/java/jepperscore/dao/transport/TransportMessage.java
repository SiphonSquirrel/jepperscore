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

/**
 * This class provides the XML message for sending events or alias across the wire.
 * @author Chuck
 *
 */
@XmlRootElement(name="message")
@XmlAccessorType(XmlAccessType.NONE)
public class TransportMessage {
	/**
	 * Round of the message.
	 */
	private Round round;

	/**
	 * Event of the message.
	 */
	private Event event;

	/**
	 * @return The message content.
	 */
	@XmlElements(value = { @XmlElement(name = "round", type = Round.class),
			@XmlElement(name = "event", type = Event.class),
			@XmlElement(name = "alias", type = Alias.class) })
	@CheckForNull
	public Object getMessageContent() {
		if (round != null) {
			return round;
		}else if (event != null) {
			return event;
		} else {
			return null;
		}
	}

	/**
	 * Sets the message content.
	 * @param content The content of the message.
	 */
	public void setMessageContent(@Nonnull Object content) {
		if (content instanceof Round) {
			round = (Round) content;
		} else if (content instanceof Event) {
			event = (Event) content;
		}
	}

	/**
	 * @return The round. Represents the start or end of a round.
	 */
	@CheckForNull
	public Round getRound() {
		return round;
	}

	/**
	 * @param round The round to set. Represents the start or end of a round.
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
	 * @param event The event.
	 */
	public void setEvent(@Nullable Event event) {
		this.event = event;
	}
}
