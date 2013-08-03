package jepperscore.dao.model.adapter;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.DateTime;

/**
 * This class is used to translate between {@link String} and {@link DateTime} for XML marshalling.
 * @author Chuck
 *
 */
public class DateTimeAdapter extends XmlAdapter<String, DateTime> {

	@Override
	@CheckForNull
	public DateTime unmarshal(@Nullable String v) throws Exception {
		if (v == null) {
			return null;
		}
		return new DateTime(v);
	}

	@Override
	@CheckForNull
	public String marshal(@Nullable DateTime v) throws Exception {
		if (v == null) {
			return null;
		}
		return v.toString();
	}

}
