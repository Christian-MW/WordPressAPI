package com.wordpress.api.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public ForbiddenException(String exception) {
        super(exception);
    } 
    public ForbiddenException() {
    }   
}
