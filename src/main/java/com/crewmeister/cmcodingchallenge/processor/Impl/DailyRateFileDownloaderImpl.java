package com.crewmeister.cmcodingchallenge.processor.Impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;

import com.crewmeister.cmcodingchallenge.constant.CurrencyConstant;
import com.crewmeister.cmcodingchallenge.currency.RateFileDownloadController;
import com.crewmeister.cmcodingchallenge.processor.DailyRateFileDownloader;

/**
 * Service class for Rate File Download
 * 
 */

@Service
public class DailyRateFileDownloaderImpl implements DailyRateFileDownloader {

	private static final Logger LOGGER = LogManager.getLogger(RateFileDownloadController.class.getName());

	private RestTemplate restTemplate;

	@Value("${app.rate.file.target}")
	private String dailyRateFolder;

	@Value("${app.rate.download.url}")
	private String dailyRateUrl;

	@Value("${app.rate.download.format}")
	private String rateDownloadFormat;

	@Autowired
	public DailyRateFileDownloaderImpl(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	/**
	 * Downloads rate file from Bank's endpoint and stores in local mount
	 * 
	 * @param fileName 
	 * 		  Input file name
	 * 
	 * @return status code 
	 * 		  Integer status code
	 */

	@Override
	public Integer downloadfile(String fileName) {
		String url = dailyRateUrl + fileName + rateDownloadFormat;
		LOGGER.info("File URL - " + url);

		String targetPath = dailyRateFolder + fileName;

		Path path = Paths.get(dailyRateFolder);
		try {
			Files.createDirectories(path);
		} catch (IOException e) {
			LOGGER.error("Unable to create directory " + path);
		}

		RequestCallback requestCallback = request -> request.getHeaders()
				.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));

		// Streaming the response to file
		int statusCode = restTemplate.execute(url, HttpMethod.GET, requestCallback, clientHttpResponse -> {
			Files.copy(clientHttpResponse.getBody(), Paths.get(targetPath + CurrencyConstant.RATE_FILE_EXTENSION));
			return clientHttpResponse.getStatusCode().value();
		});

		return statusCode;

	}

}
