/**
 * This package holds all the information used to represent the output of game scraping.
 */
@XmlJavaTypeAdapter(type=DateTime.class, value=DateTimeAdapter.class)
package jepperscore.dao.model;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import jepperscore.dao.model.adapter.DateTimeAdapter;

import org.joda.time.DateTime;

