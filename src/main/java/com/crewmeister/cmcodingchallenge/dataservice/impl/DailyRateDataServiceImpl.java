package com.crewmeister.cmcodingchallenge.dataservice.impl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.crewmeister.cmcodingchallenge.dataservice.DailyRateDataService;
import com.crewmeister.cmcodingchallenge.dto.DailyRateDto;
import com.crewmeister.cmcodingchallenge.entity.DailyRate;
import com.crewmeister.cmcodingchallenge.helper.DailyRateMapper;
import com.crewmeister.cmcodingchallenge.repository.DailyRateRepository;

/**
 * Data service Implementation for Daily Rates CRUD operation.
 * 
 * Handles Caching of specific key searches
 * 
 */
@Service
public class DailyRateDataServiceImpl implements DailyRateDataService {

	private static final Logger LOGGER = LogManager.getLogger(DailyRateDataServiceImpl.class.getName());

	@Autowired
	private DailyRateRepository dailyRateRepository;

	@Autowired
	private DailyRateMapper mapper;
	
	/**
	 * Function saves all rates, read from Exchange rate file.
	 * 
	 * On every load it evicts all of old caching data.
	 * 
	 * @param List<DailyRate>
	 * 			List of <DailyRate> entity
	 * 
	 * @return List<DailyRate> 
	 * 		 Successful saved records.
	 */
	@Override
	@CacheEvict(value = "ratesCache", allEntries = true)
	public List<DailyRate> saveAllRates(List<DailyRate> rateList) {
		LOGGER.info("Flushing batchSize" + rateList.size());
		Iterable<DailyRate> response = dailyRateRepository.saveAll(rateList);
		LOGGER.info("Flushing after save ");

		List<DailyRate> entityList = new ArrayList<DailyRate>();
		response.forEach(entityList::add);
		return entityList;
	}

	/**
	 * Function to fetch all available currencies.
	 * 
	 * 
	 * @return List<String> 
	 * 		 List of currency codes.
	 */
	@Override
	public List<String> findAllCurrencies() {
		return dailyRateRepository.findDistinctCurrencies();
	}

	/**
	 * Function to fetch all available rates.
	 * 
	 * 
	 * @return List<DailyRateDto> 
	 * 		 List of <DailyRateDto> DTOs.
	 */
	@Override
	public List<DailyRateDto> findAllRates() {
		Iterable<DailyRate> dailyRateList = dailyRateRepository.findAll();
		List<DailyRate> entityList = new ArrayList<DailyRate>();
		dailyRateList.forEach(entityList::add);
		return entityList.stream().map(mapper::toDto).collect(Collectors.toList());
	}
	

	/**
	 * Function to fetch all available rates by pagination.
	 * 
	 * @param page
	 * 		 Integer page number
	 * 
	 * @param size
	 * 		 Integer page size
	 * 
	 * @param sort
	 * 		 String field name
	 * 
	 * @return List<DailyRateDto> 
	 * 		 List of <DailyRateDto> DTOs.
	 */
	@Override
	public List<DailyRateDto> findAllRatesByPage(int page, int size, String sort) {
		Pageable paging = PageRequest.of(page, size, Sort.by(sort));
		Page<DailyRate> dailyRateList = dailyRateRepository.findAll(paging);

		return dailyRateList.stream().map(mapper::toDto).collect(Collectors.toList());
	}

	/**
	 * Function to fetch all available rates with sorting.
	 * 
	 * @param sort
	 * 		 String field name
	 * 
	 * @return List<DailyRateDto> 
	 * 		 List of <DailyRateDto> DTOs.
	 */
	@Override
	public List<DailyRateDto> findAllRatesBySort(String sort) {
		Sort sortOrder = Sort.by(sort);
		Iterable<DailyRate> list = dailyRateRepository.findAll(sortOrder);
		List<DailyRate> entityList = new ArrayList<DailyRate>();
		list.forEach(entityList::add);

		return entityList.stream().map(mapper::toDto).collect(Collectors.toList());
	}
	

	/**
	 * Function to fetch rates for requested date.
	 * On 1st request it caches based on date as key.
	 * 
	 * @param date
	 * 		 <Date> rate date
	 * 
	 * @return List<DailyRateDto> 
	 * 		 List of <DailyRateDto> DTOs.
	 */
	@Override
	@Cacheable(value = "ratesCache", key = "#date")
	public List<DailyRateDto> findByRateDate(Date date) {
		LOGGER.info("Getting Rate from DB with Date {}.", date);
		List<DailyRate> dailyRateList = dailyRateRepository.findByRateDate(date);

		return dailyRateList.stream().map(mapper::toDto).collect(Collectors.toList());
	}

	/**
	 * Function to fetch rates for requested date and currency.
	 * On 1st request it caches based on date and currency code as key.
	 * 
	 * @param date
	 * 		 <Date> rate date
	 * 
	 * @param sourceCurrency
	 * 		 <String> Currency code
	 * 
	 * @return List<DailyRateDto> 
	 * 		 List of <DailyRateDto> DTOs.
	 */
	@Override
	@Cacheable(value = "ratesCache", key = "{#rateDate, #sourceCurrency}")
	public DailyRateDto findByRateDateAndSourceCurrency(Date rateDate, String sourceCurrency) {
		LOGGER.info("Getting Rate from DB with Date {}. and Currency {}.", rateDate, sourceCurrency);
		DailyRate rateEntity = dailyRateRepository.findByRateDateAndSourceCurrency(rateDate, sourceCurrency);
		return mapper.toDto(rateEntity);

	}

}
