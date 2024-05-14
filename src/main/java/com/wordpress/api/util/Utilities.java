package com.wordpress.api.util;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class Utilities {
	private static Logger log = LoggerFactory.getLogger(Utilities.class);
	
	public ResponseEntity<?> getResponseEntity(Map<String, Object> map){
		log.info("getResponseEntity.....");
		ResponseEntity<Object> response = new ResponseEntity<Object>(map,HttpStatus.OK);
		log.info("Procesando el code...");
		int code = (int) map.get("code");
		if(code == 500)
			response = new ResponseEntity<Object>(map,HttpStatus.INTERNAL_SERVER_ERROR);
		if(code == 204)
			response = new ResponseEntity<Object>(map,HttpStatus.NO_CONTENT);
		if(code == 201)
			response = new ResponseEntity<Object>(map,HttpStatus.CREATED);
		if(code == 409)
			response = new ResponseEntity<Object>(map,HttpStatus.CONFLICT);
		if(code == 401)
			response = new ResponseEntity<Object>(map,HttpStatus.UNAUTHORIZED);
		if(code == 404)
			response = new ResponseEntity<Object>(map,HttpStatus.NOT_FOUND);
		if(code == 402)
			response = new ResponseEntity<Object>(map,HttpStatus.UNAUTHORIZED);
		if(code == 411)
			response = new ResponseEntity<Object>(map,HttpStatus.LENGTH_REQUIRED);
		
		return response;
	}
}
