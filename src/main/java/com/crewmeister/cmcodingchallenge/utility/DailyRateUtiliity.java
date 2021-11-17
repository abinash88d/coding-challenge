package com.crewmeister.cmcodingchallenge.utility;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.crewmeister.cmcodingchallenge.exception.FileParsingException;

/**
 * Utility class for Date and other operation
 * 
 */

public class DailyRateUtiliity {

	private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	private DailyRateUtiliity() {

	}

	public static Date getDateValue(String inputDate) throws FileParsingException {
		java.sql.Date sqlStartDate = null;
		try {
			java.util.Date date = dateFormat.parse(inputDate);
			sqlStartDate = new java.sql.Date(date.getTime());

		} catch (ParseException e) {
			throw new FileParsingException("Unable to parse date {}." + inputDate);
		}
		return sqlStartDate;
	}

	public static String formatDate(Date inputDate) {
		return dateFormat.format(inputDate);
	}
	

}
