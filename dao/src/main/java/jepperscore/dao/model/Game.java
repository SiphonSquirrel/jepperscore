package jepperscore.dao.model;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This represents a single game element.
 *
 * @author Chuck
 */
@XmlRootElement(name = "game")
@XmlAccessorType(XmlAccessType.NONE)
public class Game {
	/**
	 * The name of the game.
	 */
	@XmlAttribute(required = true)
	@JsonProperty
	private String name;

	/**
	 * The name of the gametype.
	 */
	@XmlAttribute(required = true)
	@JsonProperty
	private String gametype;

	/**
	 * The name of the mod, if any.
	 */
	@XmlAttribute(required = false)
	@JsonProperty
	private String mod;

	/**
	 * Default constructor.
	 */
	public Game() {
	}

	/**
	 * Constructor with all arguments.
	 *
	 * @param name
	 *            The name of the game.
	 * @param gametype
	 *            The gametype.
	 * @param mod
	 *            The mod.
	 */
	public Game(String name, String gametype, String mod) {
		this.name = name;
		this.gametype = gametype;
		this.mod = mod;
	}

	/**
	 * @return The name of the game
	 */
	@Nonnull
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the game.
	 *
	 * @param name
	 *            The name of the game.
	 */
	public void setName(@Nonnull String name) {
		this.name = name;
	}

	/**
	 * @return The mod used, if any.
	 */
	@CheckForNull
	public String getMod() {
		return mod;
	}

	/**
	 * @param mod
	 *            The mod used.
	 */
	public void setMod(@Nullable String mod) {
		this.mod = mod;
	}

	/**
	 * @return the gametype
	 */
	@Nonnull
	public String getGametype() {
		return gametype;
	}

	/**
	 * @param gametype
	 *            the gametype to set
	 */
	public void setGametype(@Nonnull String gametype) {
		this.gametype = gametype;
	}

	/**
	 * @return A deep copy of this object.
	 */
	public Game copy() {
		return new Game(name, gametype, mod);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result)
				+ ((gametype == null) ? 0 : gametype.hashCode());
		result = (prime * result) + ((mod == null) ? 0 : mod.hashCode());
		result = (prime * result) + ((name == null) ? 0 : name.hashCode());
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
		Game other = (Game) obj;
		if (gametype == null) {
			if (other.gametype != null) {
				return false;
			}
		} else if (!gametype.equals(other.gametype)) {
			return false;
		}
		if (mod == null) {
			if (other.mod != null) {
				return false;
			}
		} else if (!mod.equals(other.mod)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}
}
