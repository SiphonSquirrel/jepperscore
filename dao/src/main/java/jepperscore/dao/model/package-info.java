@XmlJavaTypeAdapter(type=DateTime.class, value=DateTimeAdapter.class)
package jepperscore.dao.model;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.joda.time.DateTime;
import jepperscore.dao.model.adapter.DateTimeAdapter;
