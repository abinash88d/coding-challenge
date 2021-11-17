package com.crewmeister.cmcodingchallenge.currency;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crewmeister.cmcodingchallenge.dataservice.DailyRateDataService;
import com.crewmeister.cmcodingchallenge.entity.DailyRate;
import com.crewmeister.cmcodingchallenge.exception.FileParsingException;
import com.crewmeister.cmcodingchallenge.exception.RateFileNotFoundException;
import com.crewmeister.cmcodingchallenge.processor.DailyRateFileDownloader;
import com.crewmeister.cmcodingchallenge.processor.DailyRateFileParser;

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

		LOGGER.info("Download status fileName " + fileName + "statusCode :"+statusCode);

		if (200 == statusCode) {
			try {
				List<DailyRate> dailyRateList = dailyRateXmlParser.parseXml(fileName);
				LOGGER.info("Parsing completed " + fileName + "statusCode :"+statusCode);

				List<DailyRate> savedRecordList = dailyRateDataService.saveAllRates(dailyRateList);

				LOGGER.info("Total record processed size : " + savedRecordList.size());

			} catch (FileParsingException exception) {
				LOGGER.error("Unable to read File {}." + fileName, exception);
				throw new RateFileNotFoundException("Unable to read File " + fileName, exception);
			}
		} else{
			LOGGER.error("Unable to download File {}." + fileName);
			throw new RateFileNotFoundException("Unable to download File " + fileName);
		}

		return new ResponseEntity<String>(fileName + " downloaded successfully", HttpStatus.OK);
	}

}
