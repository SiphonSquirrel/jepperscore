package jepperscore.tools.simulator;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import jepperscore.dao.AbstractMessageSource;
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
 * This message source shows off the scoreboard.
 *
 * @author Chuck
 *
 */
public class SimulatorSource extends AbstractMessageSource implements Runnable {

	/**
	 * The logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(SimulatorSource.class);

	/**
	 * Possible bot names.
	 */
	private static final String[] NAMES = new String[] {
			"Sandee",
			"Clara",
			"Madelyn",
			"Daniell",
			"Nikki",
			"Vincenzo",
			"Arron",
			"Mildred",
			"Elsa",
			"Walker",
			"Ida",
			"Vonnie",
			"Norris",
			"Ayesha",
			"Dante",
			"Kiesha",
			"Armand",
			"Burt",
			"Lois",
			"Iva",
			"Sherika",
			"Rafaela",
			"Barrie",
			"Chassidy",
			"Earleen",
			"Leesa",
			"Arcelia",
			"Merilyn",
			"Stuart",
			"Tonisha",
			"Melisa",
			"Leonard",
			"Tona",
			"Marylyn",
			"Wanetta" };

	/**
	 * This list is used to keep track of which names were used, so none of them are used twice.
	 */
	private final ArrayList<String> nameList = new ArrayList<String>();

	/**
	 * Default constructor.
	 */
	public SimulatorSource() {
		for (String name : NAMES) {
			nameList.add(name);
		}

		Thread t = new Thread(this);
		t.setDaemon(true);
		t.start();
	}

	/**
	 * Constructor called from the main class.
	 *
	 * @param dummyArg
	 *            Dummy arg passed in.
	 */
	public SimulatorSource(String dummyArg) {
		this();
	}

	/**
	 * Sleeps and logs any exceptions.
	 *
	 * @param millis
	 *            The time to sleep.
	 */
	private void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	/**
	 * This function constructions a player list for a given game, team and player count.
	 * @param game The game.
	 * @param team The team.
	 * @param maxPlayers The number of players on that team.
	 * @return A list of score representing players.
	 */
	private ArrayList<Score> getPlayerList(Game game, Team team, int maxPlayers) {
		ArrayList<Score> retVal = new ArrayList<Score>();

		for (int i = 0; i < maxPlayers; i++) {
			Person person = new Person();
			int index = (team.getTeamName().hashCode() + i) % nameList.size();
			if (index < 0) {
				index *= -1;
				if (index >= nameList.size()) {
					index--;
				}
			}
			String name = nameList.remove(index);
			Alias alias = new Alias(team.getTeamName() + i, name, Alias.DECORATION_STYLE_PLAIN, false, team, person, game, true);
			Score score = new Score(alias, 0);
			retVal.add(score);
		}

		return retVal;
	}

	@Override
	public void run() {
		Random rnd = new Random();

		sleep(3000);

		int ticker = 0;
		Game game = new Game("Super Spaz", "team deathmatch", null);
		Round round = new Round(UUID.randomUUID().toString(), new DateTime(), null, game, "SuperMap");
		Team team1 = new Team("Red Team", 0.0f);
		Team team2 = new Team("Blue Team", 0.0f);

		int playerCount = 10 + rnd.nextInt(5);
		ArrayList<Score> teamPlayers1 = getPlayerList(game, team1, playerCount + rnd.nextInt(2));
		ArrayList<Score> teamPlayers2 = getPlayerList(game, team2, playerCount + rnd.nextInt(2));

		while (true) {
			if (ticker == 0) {
				call(new TransportMessage(round.getId(), round, round.getId()));
				call(new TransportMessage(team1, round.getId()));
				call(new TransportMessage(team2, round.getId()));

				for (Score s: teamPlayers1) {
					s.setScore(0);
					call(new TransportMessage(s, round.getId()));
				}

				for (Score s: teamPlayers2) {
					s.setScore(0);
					call(new TransportMessage(s, round.getId()));
				}
			}
			else if ((ticker % 30) == 0) {
				if (rnd.nextInt(2) == 0) {
					team1.setScore(team1.getScore() + 1);
					call(new TransportMessage(team1, round.getId()));
				} else {
					team2.setScore(team2.getScore() + 1);
					call(new TransportMessage(team2, round.getId()));
				}
			} else if (ticker > 300) {
				ticker = 0;
				round.setEnd(new DateTime());
				call(new TransportMessage(round.getId(), round, round.getId()));

				round = new Round(UUID.randomUUID().toString(), new DateTime(), null, game, "SuperMap");
				continue;
			}
			else {
				if (rnd.nextInt(10) >= 3) {
					if (rnd.nextInt(2) == 0) {
						Score s = teamPlayers1.get(rnd.nextInt(teamPlayers1.size()));

						s.setScore(s.getScore() + 1);
						call(new TransportMessage(s, round.getId()));
					} else {
						Score s = teamPlayers2.get(rnd.nextInt(teamPlayers2.size()));

						s.setScore(s.getScore() + 1);
						call(new TransportMessage(s, round.getId()));
					}
				}
			}

			sleep(500);
			ticker += 1;
		}
	}

}
