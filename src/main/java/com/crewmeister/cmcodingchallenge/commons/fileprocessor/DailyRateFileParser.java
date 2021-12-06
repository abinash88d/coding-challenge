package com.crewmeister.cmcodingchallenge.commons.fileprocessor;

import java.sql.Date;
import java.util.List;

import com.crewmeister.cmcodingchallenge.data.entity.DailyRate;
import com.crewmeister.cmcodingchallenge.exception.FileParsingException;

public interface DailyRateFileParser {

	public List<DailyRate> parseXml(String fileName) throws FileParsingException;
	
	public List<DailyRate> parseXml(String fileName, Date filterDate) throws FileParsingException;

	public Boolean verifyFile(String fileName, Date filterDate) throws FileParsingException;
}
