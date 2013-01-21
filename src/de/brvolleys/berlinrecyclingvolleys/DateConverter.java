package de.brvolleys.berlinrecyclingvolleys;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateConverter {
	public final static String FORMAT = "dd.MM.yyyy";

	public static Date getDate(String dateContent) {
		Date date = null;
		try {
			date = new SimpleDateFormat(FORMAT, Locale.GERMAN)
					.parse(dateContent);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	
	public static String getString(Date date) {
		DateFormat df = new SimpleDateFormat(FORMAT, Locale.GERMAN);
		return df.format(date);
	}

}
