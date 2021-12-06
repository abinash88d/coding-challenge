package com.crewmeister.cmcodingchallenge.data.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.crewmeister.cmcodingchallenge.data.entity.DailyRate;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class DailyRateRepositoryTest {

	@Autowired
	DailyRateRepository dailyRateRepository;

	@BeforeEach
	public void setUp() {
		DailyRate rateAUD = new DailyRate();
		rateAUD.setSourceCurrency("AUD");
		rateAUD.setRateDate(Date.valueOf("2021-11-11"));
		rateAUD.setExchangeRate(new BigDecimal(0.434));
		rateAUD.setTargetCurrency("EUR");
		rateAUD.setUpdatedTimestamp(new Timestamp(System.currentTimeMillis()));
		rateAUD.setUpdatedUser("DAILY_RATE_TEST");
		dailyRateRepository.save(rateAUD);
	}

	@Test
	public void testSaveRate() {
		DailyRate rateAUD = new DailyRate();
		rateAUD.setSourceCurrency("AUD");
		rateAUD.setRateDate(Date.valueOf("2021-11-11"));
		rateAUD.setExchangeRate(new BigDecimal(0.434));
		rateAUD.setTargetCurrency("EUR");
		rateAUD.setUpdatedTimestamp(new Timestamp(System.currentTimeMillis()));
		rateAUD.setUpdatedUser("DAILY_RATE_TEST");
		dailyRateRepository.save(rateAUD);

		Iterable<DailyRate> rateList = dailyRateRepository.findAll();
		assertThat(rateList).extracting(DailyRate::getSourceCurrency).containsOnly("AUD");

	}

    @Test
	public void testFindAllRates() {
		Iterable<DailyRate> rateList = dailyRateRepository.findAll();
		assertThat(rateList).hasSize(1);

	}

	@Test
	public void testFindAllCurrencies() {
		List<String> rateList = dailyRateRepository.findDistinctCurrencies();
		assertThat(rateList.get(0)).hasToString("AUD");
	}

	@Test
	public void testFindByRateDate() {
		List<DailyRate> rateList = dailyRateRepository.findByRateDate(Date.valueOf("2021-11-11"));
		assertThat(rateList).extracting(DailyRate::getRateDate).containsOnly(Date.valueOf("2021-11-11"));
	}

	@Test
	public void testFindByRateDateAndSourceCurrency() {
		DailyRate rate = dailyRateRepository.findByRateDateAndSourceCurrency(Date.valueOf("2021-11-11"), "AUD");
		assertThat(rate.getRateDate()).isEqualTo(Date.valueOf("2021-11-11"));
		assertThat(rate.getSourceCurrency()).isEqualTo("AUD");

	}

}
