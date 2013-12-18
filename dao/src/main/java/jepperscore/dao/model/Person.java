package jepperscore.dao.model;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a single real person.
 * @author Chuck
 *
 */
@XmlRootElement(name="person")
@XmlAccessorType(XmlAccessType.NONE)
public class Person {
	/**
	 * The name of the person.
	 */
	@XmlAttribute(required=true)
	@JsonProperty
	private String name;

	/**
	 * @return The name of the person.
	 */
	@Nonnull
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the person.
	 * @param name The name.
	 */
	public void setName(@Nonnull String name) {
		this.name = name;
	}
}
