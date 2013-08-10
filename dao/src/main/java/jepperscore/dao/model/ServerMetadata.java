package jepperscore.dao.model;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class contains metadata about a server where a game is currently being played.
 * @author Chuck
 *
 */
@XmlRootElement(name="alias")
@XmlAccessorType(XmlAccessType.NONE)
public class ServerMetadata {

	/**
	 * The name of the server.
	 */
	@XmlAttribute(required=false)
	private String serverName;
	
	/**
	 * The extra metadata.
	 */
	@XmlElement(required=false)
	private Map<String,String> metadata = new HashMap<String,String>();

	/**
	 * @return The server name.
	 */
	@CheckForNull
	public String getServerName() {
		return serverName;
	}

	/**
	 * Sets the server name.
	 * @param serverName The server name.
	 */
	public void setServerName(@Nullable String serverName) {
		this.serverName = serverName;
	}

	/**
	 * @return The extra metadata, specific to the query client.
	 */
	@Nonnull
	public Map<String,String> getMetadata() {
		return metadata;
	}

	/**
	 * Sets the extra metadata, specific to the query client.
	 * @param metadata The extra metadata.
	 */
	public void setMetadata(@Nonnull Map<String,String> metadata) {
		this.metadata = metadata;
	}
	
}
