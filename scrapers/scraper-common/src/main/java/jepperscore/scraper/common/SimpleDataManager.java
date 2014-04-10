package jepperscore.scraper.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jepperscore.dao.IMessageDestination;
import jepperscore.dao.model.Alias;
import jepperscore.dao.model.Game;
import jepperscore.dao.model.Person;
import jepperscore.dao.model.Round;
import jepperscore.dao.model.Score;
import jepperscore.dao.model.Team;
import jepperscore.dao.transport.TransportMessage;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class tracks the players list across all scraping methods (Log file,
 * query, RCON, etc.).
 *
 * @author Chuck
 *
 */
public class SimpleDataManager implements PlayerManager, GameManager,
		RoundManager, TeamManager, ScoreManager {

	/**
	 * The logger.
	 */
	private Logger LOG = LoggerFactory.getLogger(SimpleDataManager.class);

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
	 * The current game.
	 */
	private Game currentGame;

	/**
	 * The current round.
	 */
	private Round currentRound;

	/**
	 * The teams.
	 */
	private Map<String, Team> teams = new HashMap<String, Team>();

	/**
	 * The scores.
	 */
	private Collection<Score> scores = new LinkedList<Score>();

	/**
	 * Wipe players on a new round?
	 */
	private boolean wipePlayers = true;

	/**
	 * The message destination.
	 */
	private IMessageDestination messageDestination;

	/**
	 * Constructor for the player manager.
	 *
	 * @param messageDestination
	 *            The message destination class to handle new messages.
	 */
	public SimpleDataManager(@Nonnull IMessageDestination messageDestination) {
		this.messageDestination = messageDestination;

		newRound();
	}

	@Override
	public void setWipePlayersOnNewRound(boolean wipePlayers) {
		this.wipePlayers = wipePlayers;
	}

	@Override
	public boolean isWipePlayersOnNewRound() {
		return this.wipePlayers;
	}

	/**
	 * This function takes an alias and merges it with the existing alias
	 * definition.
	 *
	 * @param player
	 *            The player to merge.
	 * @return The merged player record.
	 */
	@Override
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

		String newId = getInternalId(player.getId());
		if ((!player.getId().isEmpty())
				&& (!newId.equals(oldPlayer.getId()) && !newId.equals(oldPlayer
						.getId()))) {
			oldPlayer.setId(newId);
			changeDetected = true;
		}

		String newName = player.getName();
		if ((!newName.equals(oldPlayer.getName()))) {
			oldPlayer.setName(newName);
			changeDetected = true;
		}

		String newDecorationStyle = player.getDecorationStyle();
		if ((!newDecorationStyle.equals(oldPlayer.getDecorationStyle()))) {
			oldPlayer.setDecorationStyle(newDecorationStyle);
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

		boolean present = player.isPresent();
		if (present != oldPlayer.isPresent()) {
			oldPlayer.setPresent(present);
			changeDetected = true;
		}

		if (changeDetected) {
			TransportMessage msg = new TransportMessage();
			msg.setId(oldPlayer.getId());
			msg.setAlias(oldPlayer);
			Round round = currentRound;
			if (round != null) {
				msg.setSessionId(round.getId());
			}
			messageDestination.sendMessage(msg);
		}

		return oldPlayer.copy();
	}

	/**
	 * This function resets all knowledge about players.
	 */
	@Override
	public synchronized void newRound() {
		newRound(null);
	}

	/**
	 * This function resets all knowledge about players.
	 *
	 * @param r
	 *            The new round to use.
	 */
	@Override
	public synchronized void newRound(Round r) {
		if ((currentRound == null) || (r != null) || !players.isEmpty()
				|| !teams.isEmpty() || !scores.isEmpty()) {
			if (wipePlayers) {
				players.clear();
			}
			teams.clear();
			scores.clear();

			Round newRound;
			if (r == null) {
				newRound = new Round();
				newRound.setId(UUID.randomUUID().toString());
				newRound.setGame(currentGame);
			} else {
				newRound = r.copy();
			}

			provideRoundRecord(newRound);
			currentRound = newRound;
		}
	}

	/**
	 * Looks up or creates a player based on their ID.
	 *
	 * @param id
	 *            The player id.
	 * @return The player.
	 */
	@Override
	public synchronized Alias getPlayer(String id) {
		return getPlayer(id, true).copy();
	}

	/**
	 * Calculates and returns the internal ID.
	 *
	 * @param id
	 *            The ID to work with.
	 * @return The internal id.
	 */
	private String getInternalId(String id) {
		String internalId = id;
		if (!internalId.startsWith(roundPrefix)) {
			internalId = roundPrefix + id;
		}
		return internalId;
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
		String internalId = getInternalId(id);

		Alias player = players.get(internalId);
		if ((player == null) && create) {
			player = new Alias();
			player.setId(internalId);
			players.put(internalId, player);

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
	@Override
	@CheckForNull
	public synchronized Alias getPlayerByName(String name) {
		for (Alias a : players.values()) {
			if (name.equals(a.getName())) {
				return a.copy();
			}
		}
		return null;
	}

	@Override
	public synchronized Round provideRoundRecord(Round round) {
		boolean changeDetected = false;

		if (currentRound == null) {
			currentRound = round.copy();
			currentRound.setGame(currentGame);
			changeDetected = true;
		} else {
			String id = round.getId();
			if ((id != null) && (!id.equals(currentRound.getId()))) {
				currentRound.setId(round.getId());

				String oldRoundPrefix = roundPrefix;
				roundPrefix = currentRound.getId() + ":";
				if (!wipePlayers) {
					Map<String, Alias> newPlayers = new HashMap<String, Alias>();
					for (Alias player : players.values()) {
						String newId = roundPrefix
								+ player.getId().substring(
										oldRoundPrefix.length());
						player.setId(newId);
						newPlayers.put(newId, player);
					}
					players.clear();
					players.putAll(newPlayers);
				}

				changeDetected = true;
			}

			Game game = round.getGame();
			if ((game != null) && (!game.equals(currentRound.getGame()))) {
				currentRound.setGame(game.copy());
				changeDetected = true;
			}

			String map = round.getMap();
			if ((map != null) && (!map.equals(currentRound.getMap()))) {
				currentRound.setMap(map);
				changeDetected = true;
			}

			DateTime start = round.getStart();
			if ((start != null) && (!start.equals(currentRound.getStart()))) {
				currentRound.setStart(start);
				changeDetected = true;
			}

			DateTime end = round.getEnd();
			if ((end != null) && (!end.equals(currentRound.getEnd()))) {
				currentRound.setEnd(end);
				changeDetected = true;
			}
		}

		if (changeDetected) {
			TransportMessage msg = new TransportMessage();
			msg.setRound(currentRound);
			Round cRound = currentRound;
			if ((cRound != null) && (cRound.getId() != null)) {
				msg.setId(cRound.getId());
				msg.setSessionId(cRound.getId());
			}
			messageDestination.sendMessage(msg);
		}

		return currentRound.copy();
	}

	@Override
	public synchronized Round getCurrentRound() {
		if (currentRound != null) {
			return currentRound.copy();
		} else {
			return null;
		}
	}

	@Override
	public synchronized Game provideGameRecord(Game game) {
		boolean changeDetected = false;

		if (currentGame == null) {
			currentGame = game.copy();
			changeDetected = true;
		} else {
			String gameType = game.getGametype();
			if ((gameType != null)
					&& (!gameType.equals(currentGame.getGametype()))) {
				currentGame.setGametype(gameType);
				changeDetected = true;
			}

			String mod = game.getMod();
			if ((mod != null) && (!mod.equals(currentGame.getMod()))) {
				currentGame.setMod(mod);
				changeDetected = true;
			}

			String name = game.getName();
			if ((name != null) && (!name.equals(currentGame.getName()))) {
				currentGame.setName(name);
				changeDetected = true;
			}
		}

		if (changeDetected) {
			Round round = currentRound;
			if (round != null) {
				round.setGame(currentGame);

				TransportMessage msg = new TransportMessage();
				msg.setRound(round);

				msg.setSessionId(round.getId());
				messageDestination.sendMessage(msg);
			}
		}

		return currentGame.copy();
	}

	@Override
	public synchronized Game getCurrentGame() {
		if (currentGame != null) {
			return currentGame.copy();
		} else {
			return null;
		}
	}

	@Override
	public synchronized Score provideScoreRecord(Score score) {
		boolean changeDetected = false;

		if (score.getAlias() == null) {
			LOG.error("Cannot merge score without associated alias.");
			return null;
		}

		Alias alias = providePlayerRecord(score.getAlias());

		Score oldScore = getScoreForPlayer(alias, false);

		if (oldScore == null) {
			oldScore = score.copy();
			scores.add(oldScore);
			changeDetected = true;
		} else if (score.getScore() != oldScore.getScore()) {
			oldScore.setScore(score.getScore());
			changeDetected = true;
		}

		oldScore.setAlias(alias);

		if (changeDetected) {
			TransportMessage msg = new TransportMessage();
			msg.setScore(oldScore);
			Round round = currentRound;
			if (round != null) {
				msg.setSessionId(round.getId());
			}
			messageDestination.sendMessage(msg);
		}
		return score;
	}

	/**
	 * Gets the score for the alias and optionally copy it.
	 *
	 * @param player
	 *            The player to find the score for.
	 * @param doCopy
	 *            Copy the object?
	 * @return The score.
	 */
	private synchronized Score getScoreForPlayer(Alias player, boolean doCopy) {
		for (Score score : scores) {
			Alias alias = score.getAlias();
			if (alias == null) {
				continue;
			}

			String compareId = getInternalId(alias.getId());
			if (compareId != null) {
				String playerId = getInternalId(player.getId());

				if (compareId.equals(playerId)) {
					if (doCopy) {
						return score.copy();
					} else {
						return score;
					}
				}
			}

			String compareName = alias.getName();
			if ((compareName != null) && (compareName.equals(player.getName()))) {
				if (doCopy) {
					return score.copy();
				} else {
					return score;
				}
			}
		}
		return null;
	}

	@Override
	public synchronized Score getScoreForPlayer(Alias player) {
		return getScoreForPlayer(player, true);
	}

	@Override
	public synchronized Score incrementScore(Alias player, float amount) {
		Score oldScore = getScoreForPlayer(player, false);
		if (oldScore == null) {
			oldScore = new Score();
			oldScore.setAlias(player);
			oldScore.setScore(amount);
			scores.add(oldScore);
		} else {
			oldScore.setScore(oldScore.getScore() + amount);
		}

		TransportMessage transportMessage = new TransportMessage();
		transportMessage.setScore(oldScore);
		Round round = currentRound;
		if (round != null) {
			transportMessage.setSessionId(round.getId());
		}
		messageDestination.sendMessage(transportMessage);

		return oldScore.copy();
	}

	@Override
	public synchronized Team provideTeamRecord(String id, Team team) {
		boolean changeDetected = false;

		Team oldTeam = teams.get(id);
		if (oldTeam == null) {
			oldTeam = getTeamByName(team.getTeamName());
		}

		if (oldTeam == null) {
			oldTeam = team.copy();
			teams.put(id, oldTeam);
			changeDetected = true;
		} else {
			String teamName = team.getTeamName();
			if ((teamName != null) && (!teamName.equals(oldTeam.getTeamName()))) {
				oldTeam.setTeamName(teamName);
				changeDetected = true;
			}

			Float score = team.getScore();
			if ((score != null) && (!score.equals(oldTeam.getScore()))) {
				oldTeam.setScore(score);
				changeDetected = true;
			}
		}

		if (changeDetected) {
			TransportMessage msg = new TransportMessage();
			msg.setTeam(oldTeam);
			Round round = currentRound;
			if (round != null) {
				msg.setSessionId(round.getId());
			}
			messageDestination.sendMessage(msg);
		}

		return oldTeam.copy();
	}

	@Override
	public synchronized Team getTeamById(String id) {
		return teams.get(id);
	}

	@Override
	public synchronized Team getTeamByName(String name) {
		if (name == null) {
			return null;
		}

		for (Team t : teams.values()) {
			if (name.equalsIgnoreCase(t.getTeamName())) {
				return t;
			}
		}

		return null;
	}
}
