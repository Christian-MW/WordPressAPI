package com.wordpress.api.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizetException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public UnauthorizetException(String exception) {
        super(exception);
    }
}
