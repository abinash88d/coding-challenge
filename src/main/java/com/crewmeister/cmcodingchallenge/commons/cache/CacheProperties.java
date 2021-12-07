package com.crewmeister.cmcodingchallenge.commons.cache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 *  Properties bean for Redis Caching
 *
 */

@Configuration
@Getter
@Setter
public class CacheProperties {

	private int redisPort;
	private String redisHost;

	public CacheProperties(@Value("${PORT}") int redisPort,
			@Value("${REDISTOGO_URL}") String redisHost) {
		this.redisPort = redisPort;
		this.redisHost = redisHost;
	}

	public int getRedisPort() {
		return redisPort;
	}

	public void setRedisPort(int redisPort) {
		this.redisPort = redisPort;
	}

	public String getRedisHost() {
		return redisHost;
	}

	public void setRedisHost(String redisHost) {
		this.redisHost = redisHost;
	}

}
