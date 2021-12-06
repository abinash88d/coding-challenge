package com.crewmeister.cmcodingchallenge.commons.model;

import java.util.Date;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("unused")
public class Response {

	private String status;

	private String message;

	private Object data;

	public Response(HttpStatus httpStatus, String message) {
		super();
		this.status = httpStatus.name();
		this.message = message;
	}

	public Response(HttpStatus httpStatus, String message, Object data) {
		this(httpStatus, message);
		this.data = data;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

}
