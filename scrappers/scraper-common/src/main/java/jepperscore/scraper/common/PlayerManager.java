package jepperscore.scraper.common;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.jms.MessageProducer;
import javax.jms.Session;

import jepperscore.dao.model.Alias;
import jepperscore.dao.model.Game;
import jepperscore.dao.model.Person;
import jepperscore.dao.model.Team;
import jepperscore.dao.transport.TransportMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class tracks the players list across all scraping methods (Log file,
 * query, RCON, etc.).
 *
 * @author Chuck
 *
 */
public class PlayerManager {

	/**
	 * The logger.
	 */
	private Logger LOG = LoggerFactory.getLogger(PlayerManager.class);

	/**
	 * The players.
	 */
	private Map<String, Alias> players = new HashMap<String, Alias>();

	/**
	 * The UUID of the round.
	 */
	@Nonnull
	private String roundPrefix = "";

	/**
	 * The current ActiveMQ session.
	 */
	@Nonnull
	private Session session;

	/**
	 * The current ActiveMQ MessageProducer.
	 */
	@Nonnull
	private MessageProducer producer;

	/**
	 * Constructor for the player manager.
	 *
	 * @param session
	 *            The ActiveMQ {@link Session} to use.
	 * @param producer
	 *            The ActiveMQ {@link MessageProducer} to use.
	 */
	public PlayerManager(@Nonnull Session session,
			@Nonnull MessageProducer producer) {
		this.session = session;
		this.producer = producer;

		newRound();
	}

	/**
	 * This function takes an alias and merges it with the existing alias
	 * definition.
	 *
	 * @param player
	 *            The player to merge.
	 * @return The merged player record.
	 */
	@Nonnull
	public synchronized Alias providePlayerRecord(@Nonnull Alias player) {
		Alias oldPlayer = null;
		boolean changeDetected = false;

		if (!player.getId().isEmpty()) {
			oldPlayer = getPlayer(player.getId(), false);

			if (oldPlayer == null) {
				oldPlayer = getPlayer(player.getId(), true);
				changeDetected = true;
			}
		} else if (!player.getName().isEmpty()) {
			for (Alias a : players.values()) {
				if (player.getName().equals(a.getName())) {
					oldPlayer = a;
					break;
				}
			}

			if (oldPlayer == null) {
				LOG.error("Unable to find player by name, and empty ID specified.");
				return player;
			}
		} else {
			LOG.error("Can only look up player by ID or name.");
			return player;
		}

		String newId = player.getId();
		if ((!newId.equals(oldPlayer.getId()) && !(roundPrefix + newId)
				.equals(oldPlayer.getId()))) {
			oldPlayer.setId(roundPrefix + newId);
			changeDetected = true;
		}

		String newName = player.getName();
		if ((!newName.equals(oldPlayer.getName()))) {
			oldPlayer.setName(newName);
			changeDetected = true;
		}

		Team newTeam = player.getTeam();
		if ((newTeam != null) && (!newTeam.equals(oldPlayer.getTeam()))) {
			oldPlayer.setTeam(newTeam);
			changeDetected = true;
		}

		Boolean newBot = player.isBot();
		if ((newBot != null) && (!newBot.equals(oldPlayer.isBot()))) {
			oldPlayer.setBot(newBot);
			changeDetected = true;
		}

		Person newPerson = player.getPerson();
		if ((newPerson != null) && (!newPerson.equals(oldPlayer.getPerson()))) {
			oldPlayer.setPerson(newPerson);
			changeDetected = true;
		}

		Game newGame = player.getGame();
		if ((newGame != null) && (!newGame.equals(oldPlayer.getGame()))) {
			oldPlayer.setGame(newGame);
			changeDetected = true;
		}

		if (changeDetected) {
			TransportMessage msg = new TransportMessage();
			msg.setAlias(oldPlayer);
			MessageUtil.sendMessage(producer, session, msg);
		}

		return oldPlayer.copy();
	}

	/**
	 * This function resets all knowledge about players.
	 */
	public synchronized void newRound() {
		players.clear();
		roundPrefix = UUID.randomUUID().toString() + ":";
	}

	/**
	 * Looks up or creates a player based on their ID.
	 *
	 * @param id
	 *            The player id.
	 * @return The player.
	 */
	public synchronized Alias getPlayer(String id) {
		Alias retVal = getPlayer(id, true);
		if (retVal != null) {
			return retVal.copy();
		} else {
			return null;
		}
	}

	/**
	 * Looks up or optionally, creates a player based on their ID.
	 *
	 * @param id
	 *            The player id.
	 * @param create
	 *            True, to create the player if they do not yet exist.
	 * @return The player.
	 */
	private synchronized Alias getPlayer(String id, boolean create) {
		Alias player = players.get(roundPrefix + id);
		if (player == null) {
			player = players.get(id);
		}
		if ((player == null) && create) {
			player = new Alias();
			player.setId(roundPrefix + id);
			players.put(roundPrefix + id, player);

		}
		return player;
	}

	/**
	 * Looks up or creates a player based on their ID.
	 *
	 * @param name
	 *            The player name.
	 * @return The player.
	 */
	@CheckForNull
	public synchronized Alias getPlayerByName(String name) {
		for (Alias a : players.values()) {
			if (name.equals(a.getName())) {
				return a.copy();
			}
		}
		return null;
	}
}
