package com.crewmeister.cmcodingchallenge.data.service;

import java.sql.Date;
import java.util.List;

import com.crewmeister.cmcodingchallenge.commons.model.DailyRateDto;
import com.crewmeister.cmcodingchallenge.data.entity.DailyRate;

/**
 * Data service for Daily Rates CRUD operation.
 * 
 */
public interface DailyRateDataService {

	public List<DailyRate> saveAllRates(List<DailyRate> rateList);

	public List<DailyRateDto> findAllRates();

	public List<DailyRateDto> findAllRatesByPage(int page, int size, String sort);

	public List<DailyRateDto> findAllRatesBySort(String sort);

	public List<DailyRateDto> findByRateDate(Date date);

	public DailyRateDto findByRateDateAndSourceCurrency(Date rateDate, String sourceCurrency);

	public List<String> findAllCurrencies();
	
	public List<Object[]> findMaximumRateDateByCurrency();

}
