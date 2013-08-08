package jepperscore.dao.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents a score for an alias.
 * @author Chuck
 *
 */
@XmlRootElement(name="score")
@XmlAccessorType(XmlAccessType.NONE)
public class Score {
	/**
	 * The alias.
	 */
	@XmlElement(name="alias", required=true)
	private Alias alias;
	
	/**
	 * The score.
	 */
	@XmlAttribute(name="score", required=true)
	private float score;

	/**
	 * @return The alias of the score.
	 */
	public Alias getAlias() {
		return alias;
	}

	/**
	 * Sets the alias of the score.
	 * @param alias The alias.
	 */
	public void setAlias(Alias alias) {
		this.alias = alias;
	}

	/**
	 * @return The score value.
	 */
	public float getScore() {
		return score;
	}

	/**
	 * Sets the score.
	 * @param score The score.
	 */
	public void setScore(float score) {
		this.score = score;
	}
}
