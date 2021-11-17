package com.crewmeister.cmcodingchallenge.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.crewmeister.cmcodingchallenge.constant.CurrencyConstant;

/**
 * Exception handler.
 * 
 * Handles all of applications Exception and sends required HTTP response and
 * message to client.
 * 
 */
@ControllerAdvice
public class RateExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(Exception.class)
	public final ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex, WebRequest request) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put(CurrencyConstant.TIMESTAMP, LocalDateTime.now());
		body.put(CurrencyConstant.MESSAGE, CurrencyConstant.SERVER_ERROR_RESPONSE);
		return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(RateNotFoundException.class)
	public final ResponseEntity<Map<String, Object>> handleRateNotFoundException(RateNotFoundException ex,
			WebRequest request) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put(CurrencyConstant.TIMESTAMP, LocalDateTime.now());
		body.put(CurrencyConstant.MESSAGE, ex.getLocalizedMessage());
		return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(RateFileNotFoundException.class)
	public final ResponseEntity<Map<String, Object>> handleRateFileNotFoundException(RateFileNotFoundException ex,
			WebRequest request) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put(CurrencyConstant.TIMESTAMP, LocalDateTime.now());
		body.put(CurrencyConstant.MESSAGE, ex.getLocalizedMessage());
		return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public final ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex,
			WebRequest request) {
		List<String> details = ex.getConstraintViolations().parallelStream().map(e -> e.getMessage())
				.collect(Collectors.toList());

		Map<String, Object> body = new LinkedHashMap<>();
		body.put(CurrencyConstant.MESSAGE, details);

		return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
	}

}