package jepperscore.dao.transport;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import jepperscore.dao.model.Alias;
import jepperscore.dao.model.Event;

@XmlRootElement(name="message")
@XmlAccessorType(XmlAccessType.NONE)
public class TransportMessage {
	/**
	 * Event of the message.
	 */
	private Event event;

	/**
	 * The alias of message.
	 */
	private Alias alias;

	/**
	 * @return The message content.
	 */
	@XmlElements(value = { @XmlElement(name = "event", type = Event.class),
			@XmlElement(name = "alias", type = Alias.class) })
	public Object getMessageContent() {
		if (event != null)
			return event;
		else if (alias != null)
			return alias;
		else
			return null;
	}

	/**
	 * Sets the message content.
	 * @param content The content of the message.
	 */
	public void setMessageContent(Object content) {
		if (content == null)
			return;

		if (content instanceof Event)
			event = (Event) content;
		else if (content instanceof Alias)
			alias = (Alias) content;
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public Alias getAlias() {
		return alias;
	}

	public void setAlias(Alias alias) {
		this.alias = alias;
	}
}
