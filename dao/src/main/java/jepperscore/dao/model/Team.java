package jepperscore.dao.model;

import javax.annotation.CheckForNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;


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
	@JsonProperty
	private String teamName;

	/**
	 * The score.
	 */
	@XmlAttribute(name="score", required=false)
	@JsonProperty
	private Float score;

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
	public Team(String teamName, Float score) {
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
	@CheckForNull
	public Float getScore() {
		return score;
	}

	/**
	 * Sets the score.
	 * @param score The score.
	 */
	public void setScore(Float score) {
		this.score = score;
	}

	/**
	 * @return A deep copy of team.
	 */
	public Team copy() {
		return new Team(teamName, score);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result)
				+ ((teamName == null) ? 0 : teamName.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Team other = (Team) obj;
		if (teamName == null) {
			if (other.teamName != null) {
				return false;
			}
		} else if (!teamName.equals(other.teamName)) {
			return false;
		}
		return true;
	}
}
