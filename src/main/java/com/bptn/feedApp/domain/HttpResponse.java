package com.bptn.feedApp.domain;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.http.HttpStatus;

public class HttpResponse {

	// Instance variables
	private Date timeStamp;
	private int httpStatusCode; // 200, 201, 400, 500
	private HttpStatus httpStatus;
	private String reason;
	private String message;

	// Constructor
	public HttpResponse(int httpStatusCode, HttpStatus httpStatus, String reason, String message) {
		this.timeStamp = new Date(); // Initialize timestamp with the current date and time
		this.httpStatusCode = httpStatusCode;
		this.httpStatus = httpStatus;
		this.reason = reason;
		this.message = message;
	}

	// Getters
	public Date getTimeStamp() {
		return timeStamp;
	}

	public int getHttpStatusCode() {
		return httpStatusCode;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	public String getReason() {
		return reason;
	}

	public String getMessage() {
		return message;
	}

	// Override toString() method
	@Override
	public String toString() {
		String formattedTimeStamp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(this.timeStamp);
		return "HttpResponse [timeStamp=" + formattedTimeStamp + ", httpStatusCode=" + httpStatusCode + ", httpStatus="
				+ httpStatus + ", reason=" + reason + ", message=" + message + "]";
	}
}
