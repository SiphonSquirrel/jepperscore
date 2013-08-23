package jepperscore.scraper.bf1942;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jepperscore.scraper.common.rcon.RconClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link RconClient} connects to a BF1942 RCON server.
 *
 * @author Chuck
 *
 */
public class BF1942RconClient implements RconClient {

	/**
	 * The logger.
	 */
	private Logger LOG = LoggerFactory.getLogger(BF1942RconClient.class);

	/**
	 * The hostname.
	 */
	private String host;

	/**
	 * The port.
	 */
	private int port;

	/**
	 * The username.
	 */
	private String user;

	/**
	 * The password.
	 */
	private String password;

	/**
	 * The socket.
	 */
	private Socket socket;

	/**
	 * The character set to use.
	 */
	private Charset charset;

	/**
	 * This constructor sets up the connection to the RCON server.
	 *
	 * @param host
	 *            The hostname.
	 * @param rconPort
	 *            The RCON port.
	 * @param rconUser
	 *            The RCON username.
	 * @param rconPassword
	 *            The RCON password.
	 */
	public BF1942RconClient(String host, int rconPort, String rconUser,
			String rconPassword) {
		this.host = host;
		this.port = rconPort;
		this.user = rconUser;
		this.password = rconPassword;

		charset = StandardCharsets.ISO_8859_1;
	}

	/**
	 * Encodes data for sending to the RCON server.
	 *
	 * @param data
	 *            The data to encode.
	 * @param xorBuffer
	 *            The XOR buffer.
	 * @return The encoded buffer.
	 */
	@Nonnull
	protected byte[] encode(@Nonnull byte[] data, @Nonnull byte[] xorBuffer) {
		byte[] out = new byte[data.length];

		for (int i = 0; i < data.length; i++) {
			out[i] = (byte) (data[i] ^ xorBuffer[i % xorBuffer.length]);
		}

		return out;
	}

	/**
	 * Writes a string to the {@link OutputStream}.
	 *
	 * @param os
	 *            The output stream.
	 * @param s
	 *            The string.
	 * @throws IOException
	 *             If there is a problem sending the string.
	 */
	protected void writeString(@Nonnull OutputStream os, @Nonnull String s)
			throws IOException {
		byte[] stringBuf = s.getBytes(charset);
		ByteBuffer buffer = ByteBuffer.allocate(5 + stringBuf.length);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putInt(stringBuf.length + 1);
		buffer.put(stringBuf);
		buffer.put((byte) 0);
		os.write(buffer.array());
	}

	/**
	 * Writes an encoded string to the output stream.
	 *
	 * @param os
	 *            The output stream.
	 * @param s
	 *            The string.
	 * @param xorBuffer
	 *            The XOR buffer to encode with.
	 * @throws IOException
	 *             If there is a problem sending the string.
	 */
	protected void writeEncodedString(@Nonnull OutputStream os,
			@Nonnull String s, @Nonnull byte[] xorBuffer) throws IOException {
		byte[] stringBuf = s.getBytes(charset);
		ByteBuffer buffer = ByteBuffer.allocate(5 + stringBuf.length);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putInt(stringBuf.length + 1);
		buffer.put(encode(stringBuf, xorBuffer));
		buffer.put((byte) 0);
		os.write(buffer.array());
	}

	/**
	 * Writes an integer to the {@link OutputStream}
	 *
	 * @param os
	 *            The output stream.
	 * @param i
	 *            The integer.
	 * @throws IOException
	 *             If there is a problem sending the integer.
	 */
	protected void writeInteger(@Nonnull OutputStream os, int i)
			throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putInt(i);
		os.write(buffer.array());
	}

	/**
	 * Reads an integer from the {@link InputStream}.
	 *
	 * @param is
	 *            The input stream.
	 * @return The integer.
	 * @throws IOException
	 *             If there was an error.
	 */
	protected int readInteger(@Nonnull InputStream is) throws IOException {
		byte[] bytes = new byte[4];
		if (is.read(bytes) != 4) {
			throw new IOException("Not enough data.");
		}
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		return buffer.getInt();
	}

	/**
	 * Reads a string from the {@link InputStream}.
	 *
	 * @param is
	 *            The input stream.
	 * @return The string.
	 * @throws IOException
	 *             If there was an error.
	 */
	protected String readString(@Nonnull InputStream is) throws IOException {
		int size = readInteger(is);
		byte[] bytes = new byte[size];

		int offset = 0;
		while (offset < bytes.length) {
			int read = is.read(bytes, offset, bytes.length - offset);
			if (read < 0) {
				throw new IOException("EOF reached");
			}
			offset += read;
		}
		if (bytes.length == 0) {
			return "";
		}
		return new String(bytes, 0, bytes.length - 1, charset);
	}

	/**
	 * @return An active, authenticated RCON socket.
	 */
	@CheckForNull
	protected Socket getRconSocket() {
		if ((socket == null) || (!socket.isConnected()) || (socket.isClosed())) {
			try {
				socket = new Socket(host, port);

				InputStream is = socket.getInputStream();
				OutputStream os = new BufferedOutputStream(
						socket.getOutputStream());

				byte[] xorBuffer = new byte[10];
				if (is.read(xorBuffer) != 10) {
					LOG.error("Connection Failed: Did not receive entire XOR buffer.");
					socket.close();
					return null;
				}

				writeEncodedString(os, user, xorBuffer);
				writeEncodedString(os, password, xorBuffer);
				os.flush();

				if (is.read() != 1) {
					LOG.error("Connection Failed: Login failed.");
					socket.close();
					return null;
				}
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
				return null;
			}

			LOG.info("RCON connection successful.");
		}

		return socket;
	}

	@Override
	public String[] sendCommand(String command) {
		Socket s = getRconSocket();
		if (s == null) {
			return null;
		}

		try {
			InputStream is = s.getInputStream();
			OutputStream os = new BufferedOutputStream(socket.getOutputStream());

			writeInteger(os, 2);
			writeString(os, "ConsoleMessage 0");
			writeString(os, command + "\r");
			os.flush();

			// Read block size, but do nothing with it for now.
			readInteger(is);

			String[] retVal = new String[1];
			retVal[0] = readString(is);

			return retVal;
		} catch (SocketException e) {
			try {
				s.close();
			} catch (IOException e1) {
				// Do nothing.
			}
			return null;
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			try {
				s.close();
			} catch (IOException e1) {
				// Do nothing.
			}
			return null;
		}
	}

	@Override
	public void disconnect() {
		if ((socket != null) && (socket.isConnected()) && (!socket.isClosed())) {
			try {
				socket.close();
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

}
