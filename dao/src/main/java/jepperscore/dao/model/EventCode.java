package jepperscore.dao.model;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents the extended details of an event.
 * @author Chuck
 *
 */
@XmlRootElement(name="eventCode")
@XmlAccessorType(XmlAccessType.NONE)
public class EventCode {
	/**
	 * The event code for a kill.
	 */
	public static final String EVENT_CODE_KILL = "kill";

	/**
	 * The event code for a team kill.
	 */
	public static final String EVENT_CODE_TEAMKILL = "teamkill";

	/**
	 * The event code for an objective.
	 */
	public static final String EVENT_CODE_OBJECTIVE = "objective";

	/**
	 * The code.
	 */
	@XmlAttribute(required=true)
	@JsonProperty
	private String code;

	/**
	 * The object of the event.
	 */
	@XmlAttribute(required=false)
	@JsonProperty
	private String object;

	/**
	 * Reserved for future use.
	 */
	@XmlAttribute(required=false)
	@JsonProperty
	private String extra;

	/**
	 * @return The code of the event.
	 */
	@Nonnull
	public String getCode() {
		if (code == null) {
			return "";
		}
		return code;
	}

	/**
	 * Sets the extended code for the event.
	 * @param code The code.
	 */
	public void setCode(@Nonnull String code) {
		this.code = code;
	}

	/**
	 * @return The object (weapon, capture point, etc.) used during the event.
	 */
	@CheckForNull
	public String getObject() {
		return object;
	}

	/**
	 * Sets the object (weapon, capture point, etc.) used during the event.
	 * @param object The object used.
	 */
	public void setObject(@Nullable String object) {
		this.object = object;
	}

	/**
	 * @return Extra information that does not have a place, but might be important or interesting later.
	 */
	@CheckForNull
	public String getExtra() {
		return extra;
	}

	/**
	 * Sets extra information that may be interesting later.
	 * @param extra The extra information.
	 */
	public void setExtra(@Nullable String extra) {
		this.extra = extra;
	}
}
