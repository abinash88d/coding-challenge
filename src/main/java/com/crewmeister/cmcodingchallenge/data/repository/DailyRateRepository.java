package com.crewmeister.cmcodingchallenge.data.repository;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.crewmeister.cmcodingchallenge.data.entity.DailyRate;

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
	
	@Query("SELECT a.sourceCurrency as sourceCurrency, MAX(a.rateDate) as rateDate FROM DailyRate a GROUP BY a.sourceCurrency")
	List<Object[]> findMaximumRateDateByCurrency();


}
