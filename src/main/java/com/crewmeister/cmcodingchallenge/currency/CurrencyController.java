package com.crewmeister.cmcodingchallenge.currency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.crewmeister.cmcodingchallenge.constant.CurrencyConstant;
import com.crewmeister.cmcodingchallenge.dataservice.DailyRateDataService;
import com.crewmeister.cmcodingchallenge.dto.DailyRateDto;
import com.crewmeister.cmcodingchallenge.exception.RateNotFoundException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Rate Controller for Daily Exchange Rate Operations
 * 
 * Service : /currencies GET Service to fetch all available currencies
 * 
 * Service : /rates GET Service to fetch all available rates for all the days
 * Supports pagination Supports sorting
 * 
 * Service : /rates/{date} GET Service to fetch rates for specified date
 * 
 * Service : /convertcurrency/date/{date}/currency/{currency}/amount/{amount}
 * GET Service to convert currency with provided date and FX Rate
 * 
 */

@Api(value = "Currency Controller")
@ApiResponses(value = { @ApiResponse(code = 200, message = CurrencyConstant.API_DOC_SUCCESS),
		@ApiResponse(code = 401, message = CurrencyConstant.API_DOC_NOT_AUTH),
		@ApiResponse(code = 403, message = CurrencyConstant.API_DOC_FORBIDDEN),
		@ApiResponse(code = 404, message = CurrencyConstant.API_DOC_NOT_FOUND) })

@RestController()
@RequestMapping("/api")
@Validated
public class CurrencyController {

	private static final Logger LOGGER = LogManager.getLogger(RateFileDownloadController.class.getName());

	@Autowired
	private DailyRateDataService dailyRateDataService;

	/**
	 * GET Service to fetch all available currencies
	 * 
	 */

	@ApiOperation(value = CurrencyConstant.API_DOC_CURRENCIES_VALUE, response = Iterable.class, tags = CurrencyConstant.API_DOC_CURRENCY_TAG)
	@GetMapping("/currencies")
	public ResponseEntity<List<String>> getCurrencies() {
		LOGGER.info("Inside get currencies service call ");
		List<String> currencyList = dailyRateDataService.findAllCurrencies();
		return new ResponseEntity<List<String>>(currencyList, HttpStatus.OK);
	}

	/**
	 * Get Rates Service to fetch all the rates for available date
	 * 
	 * @param page
	 * @param perPage
	 * @param sortBy
	 */
	@ApiOperation(value = CurrencyConstant.API_DOC_ALL_RATES_VALUE, response = DailyRateDto.class, tags = CurrencyConstant.API_DOC_ALL_RATES_TAG)
	@GetMapping("/rates")
	public ResponseEntity<List<DailyRateDto>> getRates(@RequestParam Optional<Integer> page,
			@RequestParam Optional<Integer> perPage, @RequestParam Optional<String> sortBy) {
		LOGGER.info("Inside get rates service call for page size {}." + perPage);

		List<DailyRateDto> rateList = Collections.emptyList();
		if (page.isPresent() && perPage.isPresent() && sortBy.isPresent()) {
			rateList = dailyRateDataService.findAllRatesByPage(page.get(), perPage.get(), sortBy.get());
		} else if (page.isPresent() && perPage.isPresent()) {
			rateList = dailyRateDataService.findAllRatesByPage(page.get(), perPage.get(),
					CurrencyConstant.DEFAULT_RATE_SORT_FIELD);
		} else if (sortBy.isPresent()) {
			rateList = dailyRateDataService.findAllRatesBySort(sortBy.get());
		} else {
			rateList = dailyRateDataService.findAllRates();
		}

		if (null != rateList && rateList.isEmpty()) {
			throw new RateNotFoundException(CurrencyConstant.RESPONSE_NO_RECORD_FOUND);
		}

		return new ResponseEntity<List<DailyRateDto>>(rateList, HttpStatus.OK);
	}

	/**
	 * Get Rates Service for specified Date
	 * 
	 * @param date
	 * 
	 */

	@ApiOperation(value = CurrencyConstant.API_DOC_RATES_BY_DATE_VALUE, response = DailyRateDto.class, tags = CurrencyConstant.API_DOC_RATES_BY_DATE_TAG)
	@GetMapping("/rates/{date}")
	public ResponseEntity<List<DailyRateDto>> getRatesByDate(@PathVariable Date date) {
		LOGGER.info("Inside get rates by date service call for date {}." + date);

		List<DailyRateDto> rateList = dailyRateDataService.findByRateDate(date);

		if (rateList.isEmpty()) {
			throw new RateNotFoundException(CurrencyConstant.RESPONSE_NO_RECORD_FOUND_FOR_DATE + date);
		} else if (null != rateList.get(0).getHolidayStatus() && rateList.get(0).getHolidayStatus().equals("K")) {
			throw new RateNotFoundException(CurrencyConstant.RESPONSE_NO_RECORD_FOR_HOLIDAY + date);
		}
		return new ResponseEntity<List<DailyRateDto>>(rateList, HttpStatus.OK);

	}

	/**
	 * Currency conversion Service
	 * 
	 * @param date
	 * @param currency Code
	 * @param amount
	 * 
	 */

	@ApiOperation(value = CurrencyConstant.API_DOC_CONVERT_CURRENCY_VALUE, response = Map.class, tags = CurrencyConstant.API_DOC_CONVERT_CURRENCY_TAG)
	@GetMapping("/convertcurrency/date/{date}/currency/{currency}/amount/{amount}")
	public ResponseEntity<Map<String, String>> convertCurrency(@PathVariable Date date,
			@NotBlank(message = CurrencyConstant.REQUEST_VALIDATION_CURRENCY_MISSING) @Size(min = 3, max = 3, message = CurrencyConstant.REQUEST_INVALID_CURRENCY) @PathVariable String currency,
			@Min(value = 1, message = CurrencyConstant.REQUEST_VALIDATION_INVALID_AMOUNT) @PathVariable BigDecimal amount) {

		LOGGER.info("Calculating converstion rate with Date {}.", date);
		DailyRateDto rateDto = dailyRateDataService.findByRateDateAndSourceCurrency(date, currency);

		if (null == rateDto) {
			LOGGER.warn("Inside convertCurrency, no record found for date {}." + date + " curreny {}." + currency);
			throw new RateNotFoundException(CurrencyConstant.RESPONSE_NO_RECORD_FOUND_FOR_DATE + date
					+ CurrencyConstant.RESPONSE_CURRENCY + currency);
		} else if (null != rateDto.getHolidayStatus() && rateDto.getHolidayStatus().equals("K")) {
			throw new RateNotFoundException(CurrencyConstant.RESPONSE_NO_RECORD_FOR_HOLIDAY + date);
		}

		BigDecimal result = amount.divide(rateDto.getExchangeRate(), 2, RoundingMode.HALF_UP);
		Map<String, String> resultMap = new HashMap<String, String>();
		resultMap.put("result", result.toPlainString());

		return new ResponseEntity<Map<String, String>>(resultMap, HttpStatus.OK);
	}

}
