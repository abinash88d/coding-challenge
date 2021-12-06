package com.crewmeister.cmcodingchallenge.commons.helper;

import org.springframework.stereotype.Component;

import com.crewmeister.cmcodingchallenge.commons.model.DailyRateDto;
import com.crewmeister.cmcodingchallenge.data.entity.DailyRate;

/**
 * Mapper class for Entity to DTO mapping.
 * 
 * 
 */
@Component
public class DailyRateMapper {

	public DailyRateDto toDto(DailyRate entity) {
		DailyRateDto dto = null;
		if (null != entity) {
		    dto = new DailyRateDto();
			dto.setId(entity.getId());
			dto.setSourceCurrency(entity.getSourceCurrency());
			dto.setTargetCurrency(entity.getTargetCurrency());
			dto.setRateDate(entity.getRateDate());
			dto.setExchangeRate(entity.getExchangeRate());
			dto.setExchangeRateDifference(entity.getExchangeRateDifference());
			dto.setHolidayStatus(entity.getHolidayStatus());
			dto.setUpdatedUser(entity.getUpdatedUser());
			dto.setUpdatedTimestamp(entity.getUpdatedTimestamp());
		}
		return dto;

	}
}
