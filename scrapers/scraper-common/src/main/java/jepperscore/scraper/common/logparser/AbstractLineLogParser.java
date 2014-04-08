package jepperscore.scraper.common.logparser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a base class for any log parser that wants to read the log stream
 * line by line.
 *
 * @author Chuck
 *
 */
public abstract class AbstractLineLogParser implements Runnable {

	/**
	 * The log.
	 */
	private static Logger LOG = LoggerFactory
			.getLogger(AbstractLineLogParser.class);

	/**
	 * The stream to read log entries from.
	 */
	private InputStream stream;

	/**
	 * Should skip to the end of files to start reading entries.
	 */
	private boolean skipToEnd;

	/**
	 * The {@link Charset} to use.
	 */
	private Charset charset;

	/**
	 * A counter to keep track of the line.
	 */
	private long lineCounter = 0;
	
	/**
	 * Creates the log parsers.
	 *
	 * @param stream
	 *            The stream to use.
	 * @param charset
	 *            The {@link Charset} to use.
	 * @param skipToEnd
	 *            True to skip to the end of the file before raising events.
	 */
	protected AbstractLineLogParser(InputStream stream, Charset charset,
			boolean skipToEnd) {
		this.stream = stream;
		this.charset = charset;
		this.skipToEnd = skipToEnd;
	}

	/**
	 * Handles a new line.
	 *
	 * @param line
	 *            The line to handle.
	 */
	protected abstract void handleNewLine(String line);

	@Override
	public void run() {
		boolean startUp = skipToEnd;

		try {
			byte[] buf = new byte[1024 * 8];
			StringBuilder sb = new StringBuilder();
			int offset = 0;
			while (true) {
				int available = stream.available();
				if (available > 0) {
					int amount = stream.read(buf, 0, Math.min(available, buf.length));
					if (amount < 0) {
						break;
					}
					sb.append(new String(buf, 0, amount, charset));

					while (offset < sb.length()) {
						char c = sb.charAt(offset);
						if ((c == '\n') || (c == '\r')) {
							if (offset > 0) {
								String s = sb.substring(0, offset);

								lineCounter++;
								if (!startUp) {
									handleNewLine(s);
								}
							}
							sb.delete(0, offset + 1);

							offset = 0;
						} else {
							offset++;
						}
					}
				} else {
					Thread.sleep(10);
				}
				startUp = false;
			}
		} catch (IOException | InterruptedException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	/**
	 * @return The line number that was just read.
	 */
	protected long getLineNumber() {
		return lineCounter;
	}

}
