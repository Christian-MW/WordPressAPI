package com.wordpress.api.dto.response;

import java.util.List;

public class GoogleGetDataResponse {
    private List<List<String>> objectResult;
    private String message;
    private int code;
    
	public List<List<String>> getObjectResult() {
		return objectResult;
	}
	public void setObjectResult(List<List<String>> objectResult) {
		this.objectResult = objectResult;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
}
