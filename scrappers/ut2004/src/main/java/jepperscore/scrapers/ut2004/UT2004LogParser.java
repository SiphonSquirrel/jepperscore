package jepperscore.scrapers.ut2004;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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
 * This class parses UT2004 console logs.
 *
 * @author Chuck
 *
 */
public class UT2004LogParser extends AbstractLineLogParser {

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
	public UT2004LogParser(@Nonnull InputStream stream, @Nonnull Session session,
			@Nonnull MessageProducer producer,
			@Nonnull PlayerManager playerManager) {
		super(stream, StandardCharsets.UTF_8, false);
		this.session = session;
		this.producer = producer;
		this.playerManager = playerManager;
	}

	@Override
	protected void handleNewLine(String line) {
		String[] arr = line.split("\t");
		if (arr.length < 2) {
			return;
		}

		try {
			// Attempt to parse timestamp.
			Float.parseFloat(arr[0]);
		} catch (NumberFormatException e) {
			return;
		}

		String lineType = arr[1];
		switch (lineType) {
		case "SG": {
			playerManager.newRound();
			break;
		}
		case "G": {
			if (arr.length >= 3) {
				String action = arr[2];

				switch (action) {
				// 71.04 G NameChange 1 SiphonSquirrel
				case "NameChange": {
					if (arr.length >= 5) {
						String id = arr[3];
						String name = arr[4];

						Alias player = new Alias();
						if (name.startsWith("[BOT]")) {
							name = name.substring(5);
							player.setBot(true);
						}
						else {
							player.setBot(false);
						}

						player.setId(id);
						player.setName(name);

						playerManager.providePlayerRecord(player);
					}
					break;
				}

				default:
					// Ignore others
					break;
				}
			}
			break;
		}

		// 288.07 K 5 DamTypeMinigunBullet 2 FlakCannon
		case "K": {
			if (arr.length >= 6) {
				String attackerId = arr[2];
				String damageType = arr[3];
				String vicitimId = arr[4];

				// Suicide
				if (attackerId == "-1") {
					attackerId= vicitimId;
				}

				Alias attacker = playerManager.getPlayer(attackerId);
				Alias victim = playerManager.getPlayer(vicitimId);

				if ((attacker != null) && (victim != null)) {
					Event newEvent = new Event();
					newEvent.setAttacker(attacker);
					newEvent.setVictim(victim);

					EventCode eventCode = new EventCode();

					String prefix = "";
					if (attackerId.equals(vicitimId)) {
						prefix = "TK:";
						eventCode.setCode("teamkill");
					} else {
						eventCode.setCode("kill");
					}

					newEvent.setEventCode(eventCode);

					newEvent.setEventText(String.format("{attacker} [%s%s] {victim}",
							prefix, damageType));

					TransportMessage transportMessage = new TransportMessage();
					transportMessage.setEvent(newEvent);

					MessageUtil.sendMessage(producer, session, transportMessage);
				}
			}
			break;
		}
		default:
			//Ignore anything else...
			break;
		}
	}

}
