package jepperscore.dao.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.DateTime;

/**
 * @author Chuck
 *
 */
@XmlRootElement(name="event")
@XmlAccessorType(XmlAccessType.NONE)
public class Event {

	/**
	 * The timestamp of the event.
	 */
	@XmlAttribute(required=true)
	private DateTime timestamp;
	
	/**
	 * The victim of the event.
	 */
	@XmlElement(required=false)
	private Alias victim;
	
	/**
	 * The attacker of the event.
	 */
	@XmlElement(required=false)
	private Alias attacker;
	
	/**
	 * The text of the event.
	 */
	@XmlElement(required=false)
	private String eventText;
	
	/**
	 * The event code.
	 */
	@XmlElement(required=false)
	private EventCode eventCode;
	
	/**
	 * The round the event is associated with.
	 */
	@XmlElement(required=false)
	private Round round;

	public DateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(DateTime timestamp) {
		this.timestamp = timestamp;
	}

	public Alias getVictim() {
		return victim;
	}

	public void setVictim(Alias victim) {
		this.victim = victim;
	}

	public Alias getAttacker() {
		return attacker;
	}

	public void setAttacker(Alias attacker) {
		this.attacker = attacker;
	}

	public String getEventText() {
		return eventText;
	}

	public void setEventText(String eventText) {
		this.eventText = eventText;
	}

	public EventCode getEventCode() {
		return eventCode;
	}

	public void setEventCode(EventCode eventCode) {
		this.eventCode = eventCode;
	}

	public Round getRound() {
		return round;
	}

	public void setRound(Round round) {
		this.round = round;
	}
}
