package jepperscore.dao.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="alias")
@XmlAccessorType(XmlAccessType.NONE)
public class Alias {
	/**
	 * The alias.
	 */
	@XmlAttribute(required=true)
	private String name;
	
	/**
	 * The person who the alias belongs to.
	 */
	@XmlElement(required=false)
	private Person person;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}
}
