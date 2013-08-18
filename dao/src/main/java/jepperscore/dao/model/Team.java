package jepperscore.dao.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * This class represents a team.
 * @author Chuck
 *
 */
@XmlRootElement(name="team")
@XmlAccessorType(XmlAccessType.NONE)
public class Team {
	/**
	 * The team name.
	 */
	@XmlAttribute(name="teamName", required=true)
	private String teamName;

	/**
	 * The score.
	 */
	@XmlAttribute(name="score", required=false)
	private float score;

	/**
	 * Default constructor.
	 */
	public Team() {

	}

	/**
	 * Team name constructor.
	 * @param teamName The name of the team.
	 */
	public Team(String teamName) {
		this.teamName = teamName;
	}

	/**
	 * Full constructor.
	 * @param teamName The name of the team.
	 * @param score The team score.
	 */
	public Team(String teamName, float score) {
		this.teamName = teamName;
		this.score = score;
	}

	/**
	 * @return The name of the team.
	 */
	public String getTeamName() {
		return teamName;
	}

	/**
	 * Sets the name of the team.
	 * @param teamName The name of the team.
	 */
	public void setTeamName(String teamName) {
		this.teamName = teamName;
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
