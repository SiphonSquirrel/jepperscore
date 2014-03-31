package jepperscore.dao.model.converter;

import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.util.StdConverter;

public class JodaTimeToString extends StdConverter<DateTime, String> {

	@Override
	public String convert(DateTime value) {
		return value.toString();
	}

}
