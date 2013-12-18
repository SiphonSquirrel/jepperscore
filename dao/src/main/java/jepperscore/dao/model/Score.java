package jepperscore.dao.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

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
	@JsonProperty
	private Alias alias;

	/**
	 * The score.
	 */
	@XmlAttribute(name="score", required=true)
	@JsonProperty
	private float score;

	/**
	 * Default constructor.
	 */
	public Score() {

	}

	/**
	 * Constructor with all fields.
	 * @param alias The alias.
	 * @param score The score.
	 */
	public Score(Alias alias, float score) {
		this.alias = alias;
		this.score = score;
	}

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

	/**
	 * @return A deep copy of score.
	 */
	public Score copy() {
		// TODO Auto-generated method stub
		return new Score(alias.copy(), score);
	}
}
