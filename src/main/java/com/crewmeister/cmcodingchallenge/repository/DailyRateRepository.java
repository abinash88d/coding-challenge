package com.crewmeister.cmcodingchallenge.repository;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.crewmeister.cmcodingchallenge.entity.DailyRate;

/**
 * Repository for DAILY_RATE entity
 * 
 */

@Repository
public interface DailyRateRepository extends PagingAndSortingRepository<DailyRate, Long> {

	@Query("SELECT DISTINCT a.sourceCurrency FROM DailyRate a")
	List<String> findDistinctCurrencies();

	List<DailyRate> findByRateDate(Date rateDate);

	DailyRate findByRateDateAndSourceCurrency(Date rateDate, String sourceCurrency);

}
