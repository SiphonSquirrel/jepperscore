package jepperscore.jeppervcr.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents a single recorded entry.
 * @author Chuck
 *
 */
@XmlRootElement(name = "recordingEntry")
@XmlAccessorType(XmlAccessType.NONE)
public class RecordingEntry {

	/**
	 * The time offset from beginning of the recording of the entry.
	 */
	@XmlAttribute(required=true)
	private float timeOffset;

	/**
	 * The entry.
	 */
	@XmlElement(required=true)
	private String message;

	/**
	 * @return The number of seconds since start.
	 */
	public float getTimeOffset() {
		return timeOffset;
	}

	/**
	 * @param timeOffset The number of seconds since start.
	 */
	public void setTimeOffset(float timeOffset) {
		this.timeOffset = timeOffset;
	}

	/**
	 * @return The message.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message The message.
	 */
	public void setMessage(String message) {
		this.message = message;
	}
}
