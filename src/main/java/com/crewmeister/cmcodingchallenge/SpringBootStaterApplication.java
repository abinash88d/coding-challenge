package com.crewmeister.cmcodingchallenge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Boot App for Crewmeister's Daily EUR-FX Exchange rate
 * 
 * @author abinash
 *
 */

@SpringBootApplication
@EnableCaching
public class SpringBootStaterApplication {

	private static final Logger LOGGER = LogManager.getLogger(SpringBootStaterApplication.class.getName());

	public static void main(String[] args) {
		SpringApplication.run(SpringBootStaterApplication.class, args);
		LOGGER.info("Boot app started ");
		LOGGER.info("GET /api/currencies service started");
		LOGGER.info("GET /api/rates service started");
		LOGGER.info("GET /api/convertcurrency service started");

	}

}
