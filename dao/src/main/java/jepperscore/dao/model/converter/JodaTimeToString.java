package jepperscore.dao.model.converter;

import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.util.StdConverter;

/**
 * This jackson converter converts a jodatime datetime to string.
 * @author Chuck
 *
 */
public class JodaTimeToString extends StdConverter<DateTime, String> {

	@Override
	public String convert(DateTime value) {
		return value.toString();
	}

}
