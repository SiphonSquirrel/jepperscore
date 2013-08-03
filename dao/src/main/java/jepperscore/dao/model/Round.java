package jepperscore.dao.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.DateTime;

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

	public DateTime getStart() {
		return start;
	}

	public void setStart(DateTime start) {
		this.start = start;
	}

	public DateTime getEnd() {
		return end;
	}

	public void setEnd(DateTime end) {
		this.end = end;
	}

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}
	
}
