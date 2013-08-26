package jepperscore.scraper.etqw;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.jms.MessageProducer;
import javax.jms.Session;

import jepperscore.dao.model.Alias;
import jepperscore.dao.model.Event;
import jepperscore.dao.model.EventCode;
import jepperscore.dao.transport.TransportMessage;
import jepperscore.scraper.common.MessageUtil;
import jepperscore.scraper.common.PlayerManager;
import jepperscore.scraper.common.logparser.AbstractLineLogParser;

/**
 * This class parses ETQW server logs.
 *
 * @author Chuck
 *
 */
public class ETQWLogParser extends AbstractLineLogParser {

	/**
	 * The player manager.
	 */
	private PlayerManager playerManager;

	/**
	 * The current ActiveMQ session.
	 */
	private Session session;

	/**
	 * The current ActiveMQ MessageProducer.
	 */
	private MessageProducer producer;

	/**
	 * This constructor parses log entries from a stream.
	 *
	 * @param stream
	 *            The stream to read.
	 * @param session
	 *            The ActiveMQ {@link Session} to use.
	 * @param producer
	 *            The ActiveMQ {@link MessageProducer} to use.
	 * @param playerManager
	 *            The player manager to use.
	 */
	public ETQWLogParser(@Nonnull InputStream stream, @Nonnull Session session,
			@Nonnull MessageProducer producer,
			@Nonnull PlayerManager playerManager) {
		super(stream, StandardCharsets.UTF_8, false);
		this.session = session;
		this.producer = producer;
		this.playerManager = playerManager;
	}

	@Override
	protected void handleNewLine(String line) {
		String logEntry = line;
		int pos = logEntry.indexOf(" : ");
		if (pos > 0) {
			logEntry = logEntry.substring(pos + 3);
		}

		Pattern killPattern = Pattern.compile("(.*) \\[(.*)\\] (.*)");
		Matcher matcher = killPattern.matcher(logEntry);
		if (matcher.matches()) {
			String attackerName = matcher.group(1);
			String weaponName = matcher.group(2);
			String victimName = matcher.group(3);

			Alias attacker = playerManager.getPlayerByName(attackerName);
			Alias victim = playerManager.getPlayerByName(victimName);

			if ((attacker != null) && (victim != null)) {
				Event newEvent = new Event();
				newEvent.setAttacker(attacker);
				newEvent.setVictim(victim);

				EventCode eventCode = new EventCode();

				String prefix = "";
				//if (attackerId.equals(vicitimId)) {
//					prefix = "TK:";
					//eventCode.setCode("teamkill");
				//} else {
					eventCode.setCode("kill");
				//}

				newEvent.setEventCode(eventCode);

				newEvent.setEventText(String.format("{attacker} [%s%s] {victim}",
						prefix, weaponName));

				TransportMessage transportMessage = new TransportMessage();
				transportMessage.setEvent(newEvent);

				MessageUtil.sendMessage(producer, session, transportMessage);
			}
		}
	}

}