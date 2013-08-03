package jepperscore.dao.model;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This represents a single game element.
 * @author Chuck
 */
@XmlRootElement(name="game")
@XmlAccessorType(XmlAccessType.NONE)
public class Game {
	/**
	 * The name of the game.
	 */
	@XmlAttribute(required=true)
	private String name;

	/**
	 * The name of the gametype.
	 */
	@XmlAttribute(required=true)
	private String gametype;

	/**
	 * The name of the mod, if any.
	 */
	@XmlAttribute(required=false)
	private String mod;

	/**
	 * @return The name of the game
	 */
	@Nonnull
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the game.
	 * @param name The name of the game.
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
	 * @param mod The mod used.
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
	 * @param gametype the gametype to set
	 */
	public void setGametype(@Nonnull String gametype) {
		this.gametype = gametype;
	}
}
