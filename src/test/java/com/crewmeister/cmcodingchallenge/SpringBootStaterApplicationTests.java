package com.crewmeister.cmcodingchallenge;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.crewmeister.cmcodingchallenge.commons.cache.CacheProperties;
import com.crewmeister.cmcodingchallenge.commons.configuration.ApplicationConfig;
import com.crewmeister.cmcodingchallenge.commons.configuration.HttpClientConfig;
import com.crewmeister.cmcodingchallenge.entrypoint.controller.CurrencyController;
import com.crewmeister.cmcodingchallenge.entrypoint.controller.RateFileDownloadController;

@SpringBootTest
class SpringBootStaterApplicationTests {

	@Autowired
	CurrencyController currencyController;

	@Autowired
	RateFileDownloadController rateFileDownloadController;

	@Autowired
	CacheProperties redisProperties;

	@Autowired
	ApplicationConfig applicationConfig;

	@Autowired
	HttpClientConfig restTemplateConfig;

	@Test
	void contextLoadsCurrencyController() {
		assertThat(currencyController).isNotNull();
	}

	@Test
	void contextLoadsFileDownloadController() {
		assertThat(rateFileDownloadController).isNotNull();
	}

	@Test
	void contextLoadsRedisProperties() {
		assertThat(redisProperties).isNotNull();
	}

	@Test
	void contextLoadsApplicationConfig() {
		assertThat(applicationConfig).isNotNull();
	}

	@Test
	void contextLoadsRestTemplateConfig() {
		assertThat(restTemplateConfig).isNotNull();
	}
}
