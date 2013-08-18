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
 *
 * @author Chuck
 *
 */
@XmlRootElement(name = "alias")
@XmlAccessorType(XmlAccessType.NONE)
public class Alias {

	/**
	 * An identifying id.
	 */
	@XmlAttribute(required = true)
	private String id;

	/**
	 * The alias.
	 */
	@XmlAttribute(required = true)
	private String name;

	/**
	 * Indicates if the alias represents a bot.
	 */
	@XmlAttribute(required = false)
	private Boolean bot = null;

	/**
	 * The team which the alias belongs to.
	 */
	@XmlElement(required = false)
	private Team team;

	/**
	 * The person which the alias belongs to.
	 */
	@XmlElement(required = false)
	private Person person;

	/**
	 * The game the alias is playing.
	 */
	@XmlElement(required = true)
	private Game game;

	/**
	 * Default constructor.
	 */
	public Alias() {

	}

	/**
	 * Full constructor.
	 *
	 * @param id
	 * @param name
	 * @param bot
	 * @param team
	 * @param person
	 * @param game
	 */
	public Alias(String id, String name, Boolean bot, Team team, Person person,
			Game game) {
		this.id = id;
		this.name = name;
		this.bot = bot;
		this.team = team;
		this.person = person;
		this.game = game;
	}

	/**
	 * @return The id of the alias.
	 */
	@Nonnull
	public String getId() {
		if (id == null) {
			return "";
		}
		return id;
	}

	/**
	 * Sets the ID.
	 *
	 * @param id
	 *            The ID of the alias.
	 */
	public void setId(@Nonnull String id) {
		this.id = id;
	}

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
	 *
	 * @param name
	 *            The alias.
	 */
	public void setName(@Nonnull String name) {
		this.name = name;
	}

	/**
	 * @return Indicates if the alias represents a bot.
	 */
	@CheckForNull
	public Boolean isBot() {
		return bot;
	}

	/**
	 * @param bot
	 *            True if the alias is a bot, false for human.
	 */
	public void setBot(@Nonnull Boolean bot) {
		this.bot = bot;
	}

	/**
	 * @return The team of the alias.
	 */
	@CheckForNull
	public Team getTeam() {
		return team;
	}

	/**
	 * @param team
	 *            The team of the alias.
	 */
	public void setTeam(@Nullable Team team) {
		this.team = team;
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
	 *
	 * @param person
	 *            The owner of the alias.
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
	 * @param game
	 *            The game the alias is playing.
	 */
	public void setGame(@Nonnull Game game) {
		this.game = game;
	}

	/**
	 * @return A copy of the alias.
	 */
	public Alias copy() {
		return new Alias(id, name, bot, team, person, game);
	}
}
