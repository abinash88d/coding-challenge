package com.crewmeister.cmcodingchallenge;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.crewmeister.cmcodingchallenge.cache.RedisProperties;
import com.crewmeister.cmcodingchallenge.config.ApplicationConfig;
import com.crewmeister.cmcodingchallenge.config.RestTemplateConfig;
import com.crewmeister.cmcodingchallenge.currency.CurrencyController;
import com.crewmeister.cmcodingchallenge.currency.RateFileDownloadController;

@SpringBootTest
class CmCodingChallengeApplicationTests {

	@Autowired
	CurrencyController currencyController;

	@Autowired
	RateFileDownloadController rateFileDownloadController;

	@Autowired
	RedisProperties redisProperties;

	@Autowired
	ApplicationConfig applicationConfig;

	@Autowired
	RestTemplateConfig restTemplateConfig;

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
