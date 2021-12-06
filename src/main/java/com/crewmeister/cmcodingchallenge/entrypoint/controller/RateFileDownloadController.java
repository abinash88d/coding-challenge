package com.crewmeister.cmcodingchallenge.entrypoint.controller;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crewmeister.cmcodingchallenge.commons.fileprocessor.DailyRateFileDownloader;
import com.crewmeister.cmcodingchallenge.commons.fileprocessor.DailyRateFileParser;
import com.crewmeister.cmcodingchallenge.commons.utility.CurrencyConstant;
import com.crewmeister.cmcodingchallenge.commons.utility.DailyRateUtiliity;
import com.crewmeister.cmcodingchallenge.data.entity.DailyRate;
import com.crewmeister.cmcodingchallenge.data.repository.StatusRepository;
import com.crewmeister.cmcodingchallenge.data.service.DailyRateDataService;
import com.crewmeister.cmcodingchallenge.exception.FileParsingException;
import com.crewmeister.cmcodingchallenge.exception.RateFileNotFoundException;

import springfox.documentation.annotations.ApiIgnore;

/**
 * Controller to down load rate files, process and persists.
 * 
 */

@RestController
@RequestMapping("/api")
@ApiIgnore
public class RateFileDownloadController {

	private static final Logger LOGGER = LogManager.getLogger(RateFileDownloadController.class.getName());

	@Autowired
	private DailyRateFileDownloader dailyRateFileDownloader;

	@Autowired
	private DailyRateFileParser dailyRateXmlParser;

	@Autowired
	private DailyRateDataService dailyRateDataService;

	@Autowired
	private StatusRepository statusRepository;

	/**
	 * Service for uploading daily Rate in to App.
	 * 
	 * @param fileName Input file name to download and process
	 * 
	 */

	@GetMapping("/file/{fileName}")
	public ResponseEntity<String> getFile(@PathVariable String fileName) {
		LOGGER.info("Downloading File {}." + fileName);
		Integer statusCode = dailyRateFileDownloader.downloadfile(fileName);

		if (200 == statusCode) {
			try {
				List<Object[]> currencyStatus = dailyRateDataService.findMaximumRateDateByCurrency();
				Boolean isRecordsUpdated = false;
				Date lastRateEntryDate = null;
				Date currentDate = null;
				String processingCurrency = null;
				for (Object[] arr : currencyStatus) {
					if (fileName.contains(String.valueOf(arr[0]))) {
						processingCurrency = String.valueOf(arr[0]);
						LOGGER.info("processingCurrency : " + processingCurrency);
						lastRateEntryDate = DailyRateUtiliity.getDateValue(String.valueOf(arr[1]));
						LOGGER.info("lastRateEntryDate : " + lastRateEntryDate);
						currentDate = DailyRateUtiliity.getLastWorkingDate(DailyRateUtiliity.getCurrentDate());
						isRecordsUpdated = (lastRateEntryDate.toLocalDate().compareTo(currentDate.toLocalDate()) == 0);
						break;
					}
				}

				List<DailyRate> dailyRateList = new ArrayList<>();
				if (lastRateEntryDate == null) {
					dailyRateList = dailyRateXmlParser.parseXml(fileName);
				} else if (!isRecordsUpdated) {
					while (lastRateEntryDate.compareTo(currentDate) != 0) {
						lastRateEntryDate = DailyRateUtiliity.addDays(lastRateEntryDate, 1);
						List<DailyRate> fetchedRateList = dailyRateXmlParser.parseXml(fileName, lastRateEntryDate);
						dailyRateList.addAll(fetchedRateList);
					}
				}

				if (!dailyRateList.isEmpty()) {
					List<DailyRate> savedRecordList = dailyRateDataService.saveAllRates(dailyRateList);
					statusRepository.updateStatus(savedRecordList.get(0).getSourceCurrency(),
							CurrencyConstant.RATE_PROCESSING_STATUS_READY);
					LOGGER.info("Total record processed size : " + savedRecordList.size());
				} else {
					statusRepository.updateStatus(processingCurrency, CurrencyConstant.RATE_PROCESSING_STATUS_READY);
					LOGGER.info("Data base is upto dated");
				}

			} catch (Exception exception) {
				LOGGER.error("ERORO" + exception);
				LOGGER.error("ERORO" + exception.getLocalizedMessage());

				LOGGER.error("Unable to read File {}." + fileName, exception);
				//throw new RateFileNotFoundException("Unable to read File " + fileName, exception);
			}
		} else {

			LOGGER.error("Unable to download File {}." + fileName);
			throw new RateFileNotFoundException("Unable to download File " + fileName);
		}

		return new ResponseEntity<String>(fileName + " downloaded successfully", HttpStatus.OK);
	}

	@GetMapping("/status")
	public ResponseEntity<Map<String, String>> getRateProcessingStatus() {
		return new ResponseEntity<Map<String, String>>(statusRepository.getAllStatus(), HttpStatus.OK);
	}

}
