package com.crewmeister.cmcodingchallenge.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.PreDestroy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.reactive.function.client.WebClient;

import com.crewmeister.cmcodingchallenge.constant.CurrencyConstant;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import redis.embedded.RedisServer;

/**
 * Boot Application configuration
 * 
 * Manages <RedisServer>. Start/Stop server with Boot Clean up Exchange Rate
 * files from local mount {dailyrate/files/}. Downloads Exchange Rate files from
 * Bank's site Asynchronously 
 *
 */

@Configuration
public class ApplicationConfig {

	private static final Logger LOGGER = LogManager.getLogger(ApplicationConfig.class.getName());

	@Value("${app.rate.download.currencies}")
	public String currencies;

	@Value("${app.rate.file.target}")
	private String dailyRateFolder;

	@Value("${app.file.api.endpoint}")
	private String dailyRateDownloadEndpoint;

	@Value("${app.rate.filename.pattern}")
	private String rateFileName;

	@Autowired
	private RedisServer redisServer;

	/**
	 * <ApplicationReadyEvent> listener
	 * 
	 * Start <RedisServer> Clean up Exchange Rate files from local mount
	 * {dailyrate/files/} Downloads Exchange Rate files from Bank's site
	 * 
	 * Invokes endpoint {/api/file/{fileName}} to download and process rate files
	 * Asynchronously
	 *
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void EventListenerExecute() {

		redisServer.start();

		Path dir = Paths.get(dailyRateFolder);
		try {
			if (Files.isDirectory(dir)) {
				Files.walk(dir).map(Path::toFile).forEach(File::delete);
			}
		} catch (IOException exception) {
			LOGGER.error(exception);
			LOGGER.error(
					"EventListenerExecute : Failed to clean Rate file directory" + exception.getLocalizedMessage());
		}

		String[] currencyList = currencies.split(CurrencyConstant.COMMA);
		for (String currency : currencyList) {
			String fileNameToDownload = rateFileName.replace(":currency", currency);
			WebClient webClient = WebClient.create();
			Mono<String> response = webClient.get().uri(dailyRateDownloadEndpoint + fileNameToDownload).retrieve()
					.bodyToMono(String.class);
			Flux.merge(response).subscribe(LOGGER::info);

		}
	}
	
	@Bean
	public javax.validation.Validator localValidatorFactoryBean() {
	    return new LocalValidatorFactoryBean();
	}
	
	/**
	 * Hook to clean resources
	 */

	@PreDestroy
	public void onShutDown() {
		if (null != redisServer) {
			redisServer.stop();
			redisServer = null;
		}
	}
}
