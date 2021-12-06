package com.crewmeister.cmcodingchallenge.dataservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.crewmeister.cmcodingchallenge.dataservice.impl.DailyRateDataServiceImpl;
import com.crewmeister.cmcodingchallenge.dto.DailyRateDto;
import com.crewmeister.cmcodingchallenge.entity.DailyRate;
import com.crewmeister.cmcodingchallenge.helper.DailyRateMapper;
import com.crewmeister.cmcodingchallenge.repository.DailyRateRepository;

@ExtendWith(MockitoExtension.class)

public class DailyRateDataServiceTest {

	@InjectMocks
	DailyRateDataServiceImpl rateService;

	@Mock
	DailyRateRepository repository;

	@Mock
	private DailyRateMapper mapper;

	private List<DailyRate> dailyRateList;

	private Page<DailyRate> dailyRatePage;

	private List<DailyRate> dailyRateListForDate;

	private List<String> currencyList;

	private DailyRateDto rateAUDDto;

	private DailyRateDto rateUSDDto;

	@BeforeEach
	public void setup() {

		currencyList = new ArrayList<>(Arrays.asList("BRL", "CAD", "CHF"));

		dailyRateList = new ArrayList<DailyRate>();
		DailyRate rateAUD = new DailyRate();
		rateAUD.setId(1);
		rateAUD.setSourceCurrency("AUD");
		rateAUD.setRateDate(Date.valueOf("2021-11-11"));
		rateAUD.setExchangeRate(new BigDecimal(0.434));

		DailyRate rateUSD = new DailyRate();
		rateUSD.setId(2);
		rateUSD.setSourceCurrency("USD");
		rateUSD.setRateDate(Date.valueOf("2021-11-12"));
		rateUSD.setExchangeRate(new BigDecimal(0.435));
		dailyRateList.add(rateAUD);
		dailyRateList.add(rateUSD);

		dailyRatePage = new PageImpl<>(dailyRateList);

		rateAUDDto = new DailyRateDto();
		rateAUDDto.setId(1);
		rateAUDDto.setSourceCurrency("AUD");
		rateAUDDto.setRateDate(Date.valueOf("2021-11-11"));
		rateAUDDto.setExchangeRate(new BigDecimal(0.434));

		rateUSDDto = new DailyRateDto();
		rateUSDDto.setId(2);
		rateUSDDto.setSourceCurrency("USD");
		rateUSDDto.setRateDate(Date.valueOf("2021-11-12"));
		rateUSDDto.setExchangeRate(new BigDecimal(0.435));

		dailyRateListForDate = new ArrayList<DailyRate>();
		dailyRateListForDate.add(rateAUD);
	}

	@Test
	public void testSaveAllRates() {

		when(repository.saveAll(dailyRateList)).thenReturn(dailyRateList);

		List<DailyRate> response = rateService.saveAllRates(dailyRateList);
		assertEquals(2, response.size());
	}

	@Test
	public void testFindAllRates() {

		when(repository.findAll()).thenReturn(dailyRateList);
		when(mapper.toDto(dailyRateList.get(0))).thenReturn(rateAUDDto);
		when(mapper.toDto(dailyRateList.get(1))).thenReturn(rateUSDDto);

		List<DailyRateDto> response = rateService.findAllRates();
		assertEquals(2, response.size());
	}

	@Test
	public void testFindAllRatesByPage() {
		Pageable paging = PageRequest.of(1, 2, Sort.by("rateDate"));
		when(repository.findAll(paging)).thenReturn(dailyRatePage);
		when(mapper.toDto(dailyRateList.get(0))).thenReturn(rateAUDDto);
		when(mapper.toDto(dailyRateList.get(1))).thenReturn(rateUSDDto);

		List<DailyRateDto> response = rateService.findAllRatesByPage(1, 2, "rateDate");
		assertEquals(2, response.size());
		assertEquals(Date.valueOf("2021-11-11"), response.get(0).getRateDate());

	}

	@Test
	public void testFindAllRatesBySort() {
		Sort sort = Sort.by("rateDate");
		when(repository.findAll(sort)).thenReturn(dailyRatePage);
		when(mapper.toDto(dailyRateList.get(0))).thenReturn(rateAUDDto);
		when(mapper.toDto(dailyRateList.get(1))).thenReturn(rateUSDDto);

		List<DailyRateDto> response = rateService.findAllRatesBySort("rateDate");
		assertEquals(2, response.size());
		assertEquals(Date.valueOf("2021-11-11"), response.get(0).getRateDate());

	}

	@Test
	public void testFindByRateDate() {
		when(repository.findByRateDate(Date.valueOf("2021-11-11"))).thenReturn(dailyRateListForDate);
		when(mapper.toDto(dailyRateList.get(0))).thenReturn(rateAUDDto);

		List<DailyRateDto> response = rateService.findByRateDate(Date.valueOf("2021-11-11"));
		assertEquals(1, response.size());
		assertEquals(Date.valueOf("2021-11-11"), response.get(0).getRateDate());

	}

	@Test
	public void testFindByRateDateAndSourceCurrency() {
		when(repository.findByRateDateAndSourceCurrency(Date.valueOf("2021-11-11"), "AUD"))
				.thenReturn(dailyRateList.get(0));
		when(mapper.toDto(dailyRateList.get(0))).thenReturn(rateAUDDto);

		DailyRateDto response = rateService.findByRateDateAndSourceCurrency(Date.valueOf("2021-11-11"), "AUD");
		assertEquals(Date.valueOf("2021-11-11"), response.getRateDate());
		assertEquals("AUD", response.getSourceCurrency());

	}

	@Test
	public void testFindAllCurrencies() {
		when(repository.findDistinctCurrencies()).thenReturn(currencyList);

		List<String> response = rateService.findAllCurrencies();
		assertEquals(3, response.size());

	}

}
