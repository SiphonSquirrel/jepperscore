package jepperscore.dao.model;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents the Alias used by a player.
 * @author Chuck
 *
 */
@XmlRootElement(name="alias")
@XmlAccessorType(XmlAccessType.NONE)
public class Alias {
	/**
	 * The alias.
	 */
	@XmlAttribute(required=true)
	private String name;

	/**
	 * The person who the alias belongs to.
	 */
	@XmlElement(required=false)
	private Person person;

	/**
	 * The game the alias is playing.
	 */
	@XmlElement(required=true)
	private Game game;

	/**
	 * @return The alias.
	 */
	@Nonnull
	public String getName() {
		if (name == null) {
			return "";
		}
		return name;
	}

	/**
	 * Sets the alias.
	 * @param name The alias.
	 */
	public void setName(@Nonnull String name) {
		this.name = name;
	}

	/**
	 * @return The person who the alias belongs to.
	 */
	@CheckForNull
	public Person getPerson() {
		return person;
	}

	/**
	 * Sets the person who the alias belongs to.
	 * @param person The owner of the alias.
	 */
	public void setPerson(@Nullable Person person) {
		this.person = person;
	}

	/**
	 * @return The game the alias is playing.
	 */
	@Nonnull
	public Game getGame() {
		return game;
	}

	/**
	 * @param game The game the alias is playing.
	 */
	public void setGame(@Nonnull Game game) {
		this.game = game;
	}
}
