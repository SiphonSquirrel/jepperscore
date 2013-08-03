package jepperscore.dao.model.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.DateTime;

public class DateTimeAdapter extends XmlAdapter<String, DateTime> {

	public DateTime unmarshal(String v) throws Exception {
		return new DateTime(v);
	}

	public String marshal(DateTime v) throws Exception {
		if (v == null)
			return null;
		return v.toString();
	}

}
