package jepperscore.dao.model.converter;

import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.util.StdConverter;

public class StringToJodaTime extends StdConverter<String, DateTime> {

	@Override
	public DateTime convert(String value) {
		return new DateTime(value);
	}

}
