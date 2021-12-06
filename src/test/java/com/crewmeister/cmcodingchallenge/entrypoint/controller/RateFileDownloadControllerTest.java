package com.crewmeister.cmcodingchallenge.entrypoint.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.crewmeister.cmcodingchallenge.commons.fileprocessor.DailyRateFileDownloader;
import com.crewmeister.cmcodingchallenge.commons.fileprocessor.DailyRateFileParser;
import com.crewmeister.cmcodingchallenge.data.entity.DailyRate;
import com.crewmeister.cmcodingchallenge.data.repository.StatusRepository;
import com.crewmeister.cmcodingchallenge.data.service.DailyRateDataService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(RateFileDownloadController.class)
public class RateFileDownloadControllerTest {
	
	@MockBean
	private DailyRateFileDownloader dailyRateFileDownloader;

	@MockBean
	private DailyRateFileParser dailyRateXmlParser;

	@MockBean
	private DailyRateDataService dailyRateDataService;
	
	@MockBean
	private StatusRepository statusRepository;

	@Autowired
	MockMvc mockMvc;
	
	private List<DailyRate> dailyRateList;

	@BeforeEach
	public void setup() {

		dailyRateList = new ArrayList<DailyRate>();
		DailyRate rateAUD = new DailyRate();
		rateAUD.setId(1);
		rateAUD.setSourceCurrency("AUD");
		rateAUD.setRateDate(Date.valueOf("2021-11-11"));
		rateAUD.setExchangeRate(new BigDecimal(0.434));

		DailyRate rateUSD = new DailyRate();
		rateUSD.setId(2);
		rateUSD.setSourceCurrency("USD");
		rateUSD.setRateDate(Date.valueOf("2021-11-11"));
		rateUSD.setExchangeRate(new BigDecimal(0.435));
		dailyRateList.add(rateAUD);
		dailyRateList.add(rateUSD);

	}
	
	@Test
	public void testGetFile() throws Exception {

		Mockito.when(dailyRateFileDownloader.downloadfile("D.BGN.EUR.BB.AC.000")).thenReturn(200);
		Mockito.when(dailyRateXmlParser.parseXml("D.BGN.EUR.BB.AC.000")).thenReturn(dailyRateList);
		Mockito.when(dailyRateDataService.saveAllRates(dailyRateList)).thenReturn(dailyRateList);

		mockMvc.perform(get("/api/file/{fileName}", "D.BGN.EUR.BB.AC.000"))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$").exists())
				.andExpect(MockMvcResultMatchers.jsonPath("$").value("D.BGN.EUR.BB.AC.000 downloaded successfully"));
	}
	
	@Test
	public void testGetFileNotFound() throws Exception {

		Mockito.when(dailyRateFileDownloader.downloadfile("D.XYZ")).thenReturn(404);

		mockMvc.perform(get("/api/file/{fileName}", "D.XYZ"))
				.andExpect(status().isNotFound())
				.andExpect(MockMvcResultMatchers.jsonPath("$").exists())
				.andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Unable to download File D.XYZ"));
	}


}
