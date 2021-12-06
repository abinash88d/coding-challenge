package com.crewmeister.cmcodingchallenge.processor;

import java.util.List;

import com.crewmeister.cmcodingchallenge.entity.DailyRate;
import com.crewmeister.cmcodingchallenge.exception.FileParsingException;

public interface DailyRateFileParser {

	public List<DailyRate> parseXml(String fileName) throws FileParsingException;

}
