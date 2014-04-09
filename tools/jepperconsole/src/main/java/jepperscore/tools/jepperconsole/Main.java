package jepperscore.tools.jepperconsole;

import java.lang.reflect.InvocationTargetException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import jepperscore.dao.IMessageCallback;
import jepperscore.dao.IMessageSource;
import jepperscore.dao.model.Alias;
import jepperscore.dao.model.Event;
import jepperscore.dao.model.EventCode;
import jepperscore.dao.model.Game;
import jepperscore.dao.model.Round;
import jepperscore.dao.model.Score;
import jepperscore.dao.model.ServerMetadata;
import jepperscore.dao.model.Team;
import jepperscore.dao.transport.TransportMessage;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the main method for the JepperConsole app.
 *
 * @author Chuck
 *
 */
public class Main implements IMessageCallback {

	/**
	 * The logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	/**
	 * Specifies the source class.
	 */
	private static final String SOURCE_CLASS_ARG = "c";

	/**
	 * Specifies the source class setup.
	 */
	private static final String SOURCE_SETUP_ARG = "s";

	/**
	 * Enables the validator.
	 */
	private static final String ENABLE_VALIDATOR_ARG = "v";

	/**
	 * True to enable warnings and validator of incoming messages.
	 */
	private static boolean validator = true;

	/**
	 * The main function.
	 *
	 * @param args
	 *            [Active MQ Connection String]
	 * @throws ParseException
	 *             Exception throw from parsing problems.
	 */
	public static void main(String[] args) throws ParseException {
		Options options = new Options();

		options.addOption(SOURCE_CLASS_ARG, true, "Specifies the source class.");
		options.addOption(SOURCE_SETUP_ARG, true,
				"Specifies the source class setup.");
		options.addOption(ENABLE_VALIDATOR_ARG, false, "Enables the validator.");

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(options, args);

		if (!cmd.hasOption(SOURCE_CLASS_ARG)
				|| !cmd.hasOption(SOURCE_SETUP_ARG)) {
			throw new RuntimeException(
					"Incorrect arguments! Need -c [Message Source Class] -s [Message Source Setup]");
		}

		String messageSourceClass = cmd.getOptionValue(SOURCE_CLASS_ARG);
		String messageSourceSetup = cmd.getOptionValue(SOURCE_SETUP_ARG);

		IMessageSource messageSource;
		try {
			messageSource = (IMessageSource) Main.class.getClassLoader()
					.loadClass(messageSourceClass).getConstructor(String.class)
					.newInstance(messageSourceSetup);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException
				| ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		messageSource.registerCallback(new Main());

		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Do nothing.
			}
		}
	}

	@Override
	public void onMessage(TransportMessage message) {
		Alias alias = message.getAlias();
		Event event = message.getEvent();
		Round round = message.getRound();
		Score score = message.getScore();
		ServerMetadata metadata = message.getServerMetadata();
		Team team = message.getTeam();

		if (alias != null) {
			handleAlias(alias);
		} else if (event != null) {
			handleEvent(event);
		} else if (round != null) {
			handleRound(round);
		} else if (score != null) {
			handleScore(score);
		} else if (metadata != null) {
			handleMetadata(metadata);
		} else if (team != null) {
			handleTeam(team);
		} else if (validator) {
			Object content = message.getMessageContent();
			if (content == null) {
				LOG.warn("Not sure what was transported, content was null");
			} else {
				LOG.warn("Not sure what was transported: "
						+ content.getClass().getSimpleName());
			}
		}

	}

	/**
	 * This function validates game messages.
	 *
	 * @param game
	 *            The game.
	 */
	private void validateGame(@Nullable Game game) {
		if (game == null) {
			return;
		}

		if (game.getName().trim().isEmpty()) {
			LOG.warn("Game name was empty!");
		}
	}

	/**
	 * This function validates server metadata messages.
	 *
	 * @param serverMetadata
	 *            The server metadata.
	 */
	private void validateMetadata(@Nullable ServerMetadata serverMetadata) {
		if (serverMetadata == null) {
			return;
		}
	}

	/**
	 * This function handles server metadata messages.
	 *
	 * @param serverMetadata
	 *            The server metadata.
	 */
	private void handleMetadata(@Nonnull ServerMetadata serverMetadata) {
		if (validator) {
			validateMetadata(serverMetadata);
		}
	}

	/**
	 * This function validates score messages.
	 *
	 * @param score
	 *            The score.
	 */
	private void validatorScore(@Nullable Score score) {
		if (score == null) {
			return;
		}
		validateAlias(score.getAlias());
	}

	/**
	 * This function handles score messages.
	 *
	 * @param score
	 *            The score.
	 */
	private void handleScore(@Nonnull Score score) {
		if (validator) {
			validatorScore(score);
		}
		LOG.info("Got score for " + score.getAlias().getName() + " of "
				+ score.getScore());
	}

	/**
	 * This function validates round messages.
	 *
	 * @param round
	 *            The round.
	 */
	private void validateRound(@Nullable Round round) {
		if (round == null) {
			return;
		}
		validateGame(round.getGame());
		if (round.getStart() == null) {
			LOG.warn("Round start time was null!");
		}
	}

	/**
	 * This function handles round messages.
	 *
	 * @param round
	 *            The round.
	 */
	private void handleRound(@Nonnull Round round) {
		if (validator) {
			validateRound(round);
		}
		if (round.getGame() != null) {
			LOG.info("Got round update for game " + round.getGame().getName()
					+ " (with mod: " + round.getGame().getMod()
					+ ") and gametype: " + round.getGame().getGametype());
		}
	}

	/**
	 * This function validates event code messages.
	 *
	 * @param eventCode
	 *            The event code.
	 */
	private void validateEventCode(@Nullable EventCode eventCode) {
		if (eventCode == null) {
			return;
		}

		if ((!EventCode.EVENT_CODE_KILL.equals(eventCode.getCode()))
				&& (EventCode.EVENT_CODE_KILL.equalsIgnoreCase(eventCode
						.getCode()))) {
			LOG.warn(eventCode.getCode()
					+ " was not properly cased! (Expected: "
					+ EventCode.EVENT_CODE_KILL + ")");
		} else if ((!EventCode.EVENT_CODE_TEAMKILL.equals(eventCode.getCode()))
				&& (EventCode.EVENT_CODE_TEAMKILL.equalsIgnoreCase(eventCode
						.getCode()))) {
			LOG.warn(eventCode.getCode()
					+ " was not properly cased! (Expected: "
					+ EventCode.EVENT_CODE_TEAMKILL + ")");
		} else if ((!EventCode.EVENT_CODE_OBJECTIVE.equals(eventCode.getCode()))
				&& (EventCode.EVENT_CODE_OBJECTIVE.equalsIgnoreCase(eventCode
						.getCode()))) {
			LOG.warn(eventCode.getCode()
					+ " was not properly cased! (Expected: "
					+ EventCode.EVENT_CODE_OBJECTIVE + ")");
		} else if (!EventCode.EVENT_CODE_KILL.equals(eventCode.getCode())
				&& !EventCode.EVENT_CODE_TEAMKILL.equals(eventCode.getCode())
				&& !EventCode.EVENT_CODE_OBJECTIVE.equals(eventCode.getCode())) {
			LOG.warn("Unrecognized event code: " + eventCode.getCode()
					+ ". Should this be standardized?");
		}
	}

	/**
	 * This function validates event messages.
	 *
	 * @param event
	 *            The event.
	 */
	private void validateEvent(@Nullable Event event) {
		if (event == null) {
			return;
		}
		validateAlias(event.getAttacker());
		validateAlias(event.getVictim());
		validateRound(event.getRound());
		validateEventCode(event.getEventCode());
	}

	/**
	 * This function handles event messages.
	 *
	 * @param event
	 *            The event.
	 */
	private void handleEvent(@Nonnull Event event) {
		if (validator) {
			validateEvent(event);
		}
		String text = event.getParsedEventText();
		if (!text.isEmpty()) {
			LOG.info(text);
		}
	}

	/**
	 * This function validates alias messages.
	 *
	 * @param alias
	 *            The alias.
	 */
	private void validateAlias(@Nullable Alias alias) {
		if (alias == null) {
			return;
		}
		validateTeam(alias.getTeam());
		validateGame(alias.getGame());
		if (alias.getId().trim().isEmpty()) {
			LOG.warn("Alias ID was empty!");
		}
	}

	/**
	 * This function handles alias messages.
	 *
	 * @param alias
	 *            The alias.
	 */
	private void handleAlias(@Nonnull Alias alias) {
		if (validator) {
			validateAlias(alias);
		}

		StringBuffer msg = new StringBuffer();
		msg.append("Got update for alias: " + alias.getName() + " ("
				+ alias.getId() + ")");
		Team team = alias.getTeam();
		if (team != null) {
			msg.append(" on team " + team.getTeamName());
		}

		LOG.info(msg.toString());
	}

	/**
	 * This function validates team messages.
	 *
	 * @param team
	 *            The team.
	 */
	private void validateTeam(@Nullable Team team) {
		if (team == null) {
			return;
		}
		if (team.getTeamName() == null) {
			LOG.warn("Team name was null!");
		}
	}

	/**
	 * This function handles team messages.
	 *
	 * @param team
	 *            The team.
	 */
	private void handleTeam(@Nonnull Team team) {
		if (validator) {
			validateTeam(team);
		}
		LOG.info("Got team update for " + team.getTeamName() + " with score "
				+ team.getScore());
	}

}
