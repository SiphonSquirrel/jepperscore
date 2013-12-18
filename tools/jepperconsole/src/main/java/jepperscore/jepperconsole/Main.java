package jepperscore.jepperconsole;

import java.lang.reflect.InvocationTargetException;

import javax.annotation.Nonnull;

import jepperscore.dao.IMessageCallback;
import jepperscore.dao.IMessageSource;
import jepperscore.dao.model.Alias;
import jepperscore.dao.model.Event;
import jepperscore.dao.model.Round;
import jepperscore.dao.model.Score;
import jepperscore.dao.model.ServerMetadata;
import jepperscore.dao.model.Team;
import jepperscore.dao.transport.TransportMessage;

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
	 * The main function.
	 *
	 * @param args
	 *            [Active MQ Connection String]
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			throw new RuntimeException(
					"Incorrect arguments! Need [Message Destination Class] [Message Destination Setup]");
		}
		String messageSourceClass = args[0];
		String messageSourceSetup = args[1];

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
		} else {
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
	 * This function handles server metadata messages.
	 *
	 * @param serverMetadata
	 *            The server metadata.
	 */
	private void handleMetadata(@Nonnull ServerMetadata serverMetadata) {

	}

	/**
	 * This function handles score messages.
	 *
	 * @param score
	 *            The score.
	 */
	private void handleScore(@Nonnull Score score) {
		LOG.info("Got score for " + score.getAlias().getName() + " of "
				+ score.getScore());
	}

	/**
	 * This function handles round messages.
	 *
	 * @param round
	 *            The round.
	 */
	private void handleRound(@Nonnull Round round) {
		if (round.getGame() != null) {
			LOG.info("Got round update for game " + round.getGame().getName()
					+ " (with mod: " + round.getGame().getMod()
					+ ") and gametype: " + round.getGame().getGametype());
		}
	}

	/**
	 * This function handles event messages.
	 *
	 * @param event
	 *            The event.
	 */
	private void handleEvent(@Nonnull Event event) {
		String text = event.getParsedEventText();
		if (!text.isEmpty()) {
			LOG.info(text);
		}
	}

	/**
	 * This function handles alias messages.
	 *
	 * @param alias
	 *            The alias.
	 */
	private void handleAlias(@Nonnull Alias alias) {
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
	 * This function handles team messages.
	 *
	 * @param team
	 *            The team.
	 */
	private void handleTeam(@Nonnull Team team) {
		LOG.info("Got team update for " + team.getTeamName() + " with score "
				+ team.getScore());
	}

}
