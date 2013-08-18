package jepperscore.scraper.common.rcon;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This interface provides the functions for talking to a game's RCON server.
 * @author Chuck
 *
 */
public interface RconClient {
	/**
	 * Sends a command to the RCON server.
	 * @param command The command to send.
	 * @return The response from the command.
	 */
	@CheckForNull
	String[] sendCommand(@Nonnull String command);

	/**
	 * Disconnects any open RCON session.
	 */
	void disconnect();
}
