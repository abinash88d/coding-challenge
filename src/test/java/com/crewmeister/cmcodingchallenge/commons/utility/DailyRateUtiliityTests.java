package com.crewmeister.cmcodingchallenge.utility;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Date;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class DailyRateUtiliityTests {

	@Test
	public void testGetDateValue() {
		Date dateResponse = DailyRateUtiliity.getDateValue("2021-10-10");
		LocalDate localDate = dateResponse.toLocalDate();
		assertThat(localDate.getDayOfMonth()).isEqualTo(10);

	}
	
	@Test
	public void testFormatDate() {
		String dateResponse = DailyRateUtiliity.formatDate(Date.valueOf("2021-11-11"));
		assertThat(dateResponse).isEqualTo("2021-11-11");

	}
}
