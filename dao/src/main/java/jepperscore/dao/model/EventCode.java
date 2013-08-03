package jepperscore.dao.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Chuck
 *
 */
@XmlRootElement(name="eventCode")
@XmlAccessorType(XmlAccessType.NONE)
public class EventCode {
	/**
	 * The code.
	 */
	@XmlAttribute(required=true)
	private String code;
	
	/**
	 * The object of the event.
	 */
	@XmlAttribute(required=false)
	private String object;
	
	/**
	 * Reserved for future use.
	 */	
	@XmlAttribute(required=false)
	private String extra;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getObject() {
		return object;
	}

	public void setObject(String object) {
		this.object = object;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}
}
